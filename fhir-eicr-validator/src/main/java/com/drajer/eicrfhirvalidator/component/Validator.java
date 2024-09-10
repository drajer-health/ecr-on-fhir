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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

			FilesystemPackageCacheManager cacheManager = new FilesystemPackageCacheManager(
					FilesystemPackageCacheManager.FilesystemPackageCacheMode.USER);
			String path1 = this.getClass().getClassLoader().getResource("packages").getPath().toString();
			String path = new ClassPathResource("packages").getURI().toString();
			Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath:/packages/*");
			File packagePath = new File(path);
			List<String> loaderSrcs = new ArrayList<>();

			Arrays.stream(resources).parallel().forEach(resource -> {
				if (resource.exists() && resource.isReadable()) {
					try (InputStream is = resource.getInputStream()) {
						String fullName = resource.getFilename();
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
						cacheManager.addPackageToCache(packageName, version, is, packageName);
						loaderSrcs.add(packageName + "#" + version);

					} catch (Exception e) {
						logger.error("Error loading resource: " + resource.getFilename(), e);
					}
				}
			});

			logger.info("Initializing HL7 Validator inside Validator");
			ValidationEngine validationEngine = getValidationEngine(definitions, null, true, fhirSpecVersion,
					cacheManager, terminologycachePath);
			logger.info("Done initializing");

			IgLoader igLoader = new IgLoader(cacheManager, validationEngine.getContext(),
					validationEngine.getVersion());
			loaderSrcs.parallelStream().forEach(loaderSrc -> {
				try {
					igLoader.loadIg(validationEngine.getIgs(), validationEngine.getBinaries(), loaderSrc, false);
				} catch (Exception e) {
					logger.error("Error loading IG: " + loaderSrc, e);
				}
			});

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
