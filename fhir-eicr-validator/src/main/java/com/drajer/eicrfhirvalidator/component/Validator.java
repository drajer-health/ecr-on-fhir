package com.drajer.eicrfhirvalidator.component;


import com.drajer.eicrfhirvalidator.configuration.EicrValidationProperties;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.validation.IgLoader;
import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds and configures the {@link ValidationEngine} bean used for full IG-profile FHIR
 * validation, loading implementation guide packages bundled under {@code classpath:packages}
 * into the FHIR package cache.
 */
@Component
public class Validator {
    private final Logger logger = LoggerFactory.getLogger(Validator.class);

    private final EicrValidationProperties properties;
    private final ResourceLoader resourceLoader;

    public static final String VERSION_5_0_0 = "5.0.0";

    public Validator(EicrValidationProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Creates and prepares the {@link ValidationEngine} used for profile-based validation:
     * loads bundled FHIR packages (found under {@code classpath:packages}) into a
     * {@link FilesystemPackageCacheManager}, registers them as implementation guides, connects
     * to the FHIR terminology server, and applies the engine's validation settings.
     *
     * <p>Disabled when the {@code eicr.fhir-validation-disable} property is {@code true}.
     *
     * @return the prepared {@link ValidationEngine}, or {@code null} if initialization fails
     */
    @Bean
    @ConditionalOnProperty(prefix = "eicr", name = "fhir-validation-disable", havingValue = "false", matchIfMissing = true)
    public ValidationEngine createValidationEngine() {
        try {
            Path terminologycachePath = Path.of(properties.getCacheDownloadFolderPath());
            logger.info("terminologycache Path:::::{}", terminologycachePath);
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

            FilesystemPackageCacheManager cacheManager =
                    new FilesystemPackageCacheManager(
                            FilesystemPackageCacheManager.FilesystemPackageCacheMode.USER);
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
                    cacheManager.addPackageToCache(
                            packageName, version, new FileInputStream(file), packageName);
                    loaderSrcs.add(packageName + "#" + version);
                }
            }

            logger.info("initializing hl7Validator inside  Validator");
            ValidationEngine validationEngine = getValidationEngine(definitions, path, true, fhirVersion, cacheManager,terminologycachePath);
            logger.info("Done initializing");


            IgLoader igLoader = new IgLoader(cacheManager, validationEngine.getContext(), validationEngine.getVersion());
            for (String loaderSrc : loaderSrcs) {
                igLoader.loadIg(validationEngine.getIgs(), validationEngine.getBinaries(), loaderSrc, false);
            }

            validationEngine.connectToTSServer(txServer, null, FhirPublication.R4);
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
                        .withTHO(false)
                        .fromSource(src)
                        .setPcm(pcm);
        return validationEngine;
    }
}
