package com.drajer.eicrfhirvalidator.component;


import com.drajer.eicrfhirvalidator.configuration.EicrValidationProperties;
import com.drajer.eicrfhirvalidator.exception.FhirServerNotAvailableException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.validation.IgLoader;
import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Builds and configures the {@link ValidationEngine} used for full IG-profile FHIR validation,
 * loading implementation guide packages bundled under {@code classpath:packages} into the FHIR
 * package cache.
 *
 * <p>Loading takes from ~30 seconds (warm package cache) to several minutes (cold cache), so it
 * runs on a background thread started at application startup instead of blocking Spring/Tomcat
 * startup. Callers obtain the engine through {@link #awaitValidationEngine()}, which waits for
 * loading to finish.
 */
@Component
public class Validator {
    private final Logger logger = LoggerFactory.getLogger(Validator.class);

    private final EicrValidationProperties properties;

    /** Disables profile-based validation entirely; the engine is never loaded. */
    @Value("${eicr.fhir-validation-disable:false}")
    private boolean fhirValidationDisabled;

    /** How long a validation request waits for a still-loading engine before giving up. */
    @Value("${eicr.validator-startup-wait-seconds:30}")
    private long startupWaitSeconds;

    private final CompletableFuture<ValidationEngine> validationEngine = new CompletableFuture<>();
    private Thread loaderThread;

    public Validator(EicrValidationProperties properties) {
        this.properties = properties;
    }

    /** Starts building the {@link ValidationEngine} on a background thread. */
    @PostConstruct
    public void startLoading() {
        if (fhirValidationDisabled) {
            logger.info("FHIR profile validation is disabled (eicr.fhir-validation-disable=true)");
            validationEngine.completeExceptionally(
                    new FhirServerNotAvailableException("FHIR profile validation is disabled"));
            return;
        }
        loaderThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            ValidationEngine engine;
            try {
                engine = createValidationEngine();
            } catch (Throwable t) {
                // createValidationEngine() only catches Exception; an Error must still settle the
                // future, or requests would wait out the timeout and report "still loading"
                logger.error("FHIR validator failed to initialize", t);
                engine = null;
            }
            if (engine == null) {
                validationEngine.completeExceptionally(new FhirServerNotAvailableException(
                        "FHIR validator failed to initialize; see the application log for the cause"));
                return;
            }
            validationEngine.complete(engine);
            logger.info("FHIR validator ready: IG packages loaded in {} s",
                    (System.currentTimeMillis() - startTime) / 1000);
        }, "fhir-validator-init");
        // Daemon so a still-loading engine never blocks Tomcat shutdown or undeploy
        loaderThread.setDaemon(true);
        loaderThread.start();
        logger.info("FHIR validator loading IG packages in the background; "
                + "profile validation requests will wait up to {} s for it", startupWaitSeconds);
    }

    /** Stops a still-running load when the application is shut down or undeployed. */
    @PreDestroy
    public void stopLoading() {
        if (loaderThread != null && loaderThread.isAlive()) {
            loaderThread.interrupt();
        }
    }

    /**
     * Returns the prepared {@link ValidationEngine}, waiting up to
     * {@code eicr.validator-startup-wait-seconds} if it is still loading.
     *
     * @return the prepared engine
     * @throws FhirServerNotAvailableException if validation is disabled, loading failed, or the
     *     engine is still loading after the wait
     */
    public ValidationEngine awaitValidationEngine() {
        try {
            return validationEngine.get(startupWaitSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new FhirServerNotAvailableException(
                    "FHIR validator is still loading IG packages; retry shortly");
        } catch (ExecutionException e) {
            throw (FhirServerNotAvailableException) e.getCause();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FhirServerNotAvailableException("Interrupted while waiting for the FHIR validator", e);
        }
    }

    /**
     * Creates and prepares the {@link ValidationEngine} used for profile-based validation:
     * loads bundled FHIR packages (found under {@code classpath:packages}) into a
     * {@link FilesystemPackageCacheManager}, registers them as implementation guides, connects
     * to the FHIR terminology server, and applies the engine's validation settings.
     *
     * @return the prepared {@link ValidationEngine}, or {@code null} if initialization fails
     */
    ValidationEngine createValidationEngine() {
        try {
            Path terminologycachePath = Path.of(properties.getCacheDownloadFolderPath()).toAbsolutePath();
            // Keep this folder across restarts: an empty cache means re-extracting ~120 MB of
            // packages (minutes) instead of reusing them (seconds)
            logger.info("FHIR package/terminology cache folder: {}", terminologycachePath);
            final String fhirSpecVersion = "4.0";
            final String definitions =
                    VersionUtilities.packageForVersion(fhirSpecVersion)
                            + "#"
                            + VersionUtilities.getCurrentVersion(fhirSpecVersion);
            logger.info("Definitions:::::{}", definitions);
            final String txServer = true ? "http://tx.fhir.org" : null;
            final String fhirVersion = "4.0.1";

            System.setProperty("user.home", terminologycachePath.toString());
            Path cachePath = Paths.get(terminologycachePath+"/.fhir/packages");
            Files.createDirectories(cachePath);

            // Bundled hl7.fhir.r4.core and hl7.fhir.xver-extensions are base/extension
            // packages that ValidationEngineBuilder#fromSource(definitions) below loads
            // directly out of the cache; they are not implementation guides, so they are
            // added to the cache but excluded from loaderSrcs.
            final Set<String> baseFhirPackages = Set.of("hl7.fhir.r4.core", "hl7.fhir.xver-extensions");

            FilesystemPackageCacheManager cacheManager =
                    new FilesystemPackageCacheManager.Builder()
                            .withCacheFolder(cachePath.toString())
                            // No remote package servers: resolve only from the local cache
                            // (pre-populated above) so the validator works fully offline.
                            .withPackageServers(Collections.emptyList())
                            .build();
            String path = this.getClass().getClassLoader().getResource("packages").getPath().toString();
            File packagePath = new File(path);
            List<String> loaderSrcs = new ArrayList<>();
            if (packagePath.exists() && packagePath.isDirectory() && packagePath.listFiles().length > 0) {
                for (File file : packagePath.listFiles()) {
                    String fullName = file.getName();
                    String fileName = fullName.substring(0, fullName.lastIndexOf("."));
                    String[] parts = fullName.split("\\W+");
                    String version = "";
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (parts.length - i <= 4) {
                            version += parts[i] + ".";
                        }
                    }
                    version = version.substring(0, version.length() - 1);
                    String packageName = fileName.replace(version, "");
                    packageName = packageName.substring(0, packageName.length() - 1);
                    if (!cacheManager.packageExists(packageName, version)) {
                        cacheManager.addPackageToCache(
                                packageName, version, new FileInputStream(file), packageName);
                    }
                    if (!baseFhirPackages.contains(packageName)) {
                        loaderSrcs.add(packageName + "#" + version);
                    }
                }
            }

            logger.info("initializing hl7Validator inside  Validator");
            ValidationEngine validationEngine = getValidationEngine(definitions, path, true, fhirVersion, cacheManager,terminologycachePath);
            logger.info("Done initializing");


            IgLoader igLoader = new IgLoader(cacheManager, validationEngine.getContext(), validationEngine.getVersion());
            for (String loaderSrc : loaderSrcs) {
                // Loading an IG also loads its dependencies, so most bundled packages are
                // already loaded by the time the loop reaches them. Skip those instead of
                // re-reading them; load order (and so which version of a shared canonical
                // URL wins) is unchanged.
                String[] nameAndVersion = loaderSrc.split("#");
                if (validationEngine.getContext().hasPackage(nameAndVersion[0], nameAndVersion[1])) {
                    logger.debug("Skipping {}: already loaded as a dependency", loaderSrc);
                    continue;
                }
                igLoader.loadIg(validationEngine.getIgs(), validationEngine.getBinaries(), loaderSrc, false);
            }

            validationEngine.connectToTSServer(txServer, null, FhirPublication.R4, false);
            validationEngine.setAnyExtensionsAllowed(true);
            validationEngine.setHintAboutNonMustSupport(true);
            validationEngine.setNoExtensibleBindingMessages(true);
            validationEngine.setNoInvariantChecks(true);
            validationEngine.setAssumeValidRestReferences(true);
            validationEngine.setDebug(true);

            validationEngine.prepare();

            return validationEngine;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * Builds a {@link ValidationEngine} for the given FHIR version, sourced from the given
     * package/definitions reference.
     *
     * @param src the package definitions source, e.g. {@code "hl7.fhir.r4.core#4.0.1"}
     * @param path filesystem path used as the package loading context
     * @param canRunWithoutTerminologyServer whether the engine may operate without a reachable
     *     terminology server
     * @param vString the FHIR version string, e.g. {@code "4.0.1"}
     * @param pcm the package cache manager to use
     * @param terminologycachePath filesystem path for the terminology cache
     * @return a configured but not-yet-prepared {@link ValidationEngine}
     * @throws Exception if the engine cannot be built from the given source
     */
    public static ValidationEngine getValidationEngine(
            String src,
            String path,
            boolean canRunWithoutTerminologyServer,
            String vString,
            FilesystemPackageCacheManager pcm,
            Path terminologycachePath)
            throws Exception {

        final ValidationEngine validationEngine =
                new ValidationEngine.ValidationEngineBuilder()
                        .withCanRunWithoutTerminologyServer(canRunWithoutTerminologyServer)
                        .withVersion(vString)
                        .withTerminologyCachePath(terminologycachePath.toString())
                        .withNoTerminologyServer()
                        .fromSource(src)
                        .setPcm(pcm);
        return validationEngine;
    }
}
