package com.drajer.eicrfhirvalidator.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties bound from the {@code eicr.*} prefix in {@code application.yml}.
 */
@Configuration
@ConfigurationProperties(prefix = "eicr")
@ConditionalOnResource(resources = {"classpath:application.yml"})
public class EicrValidationProperties {

    /** Filesystem path used as the FHIR package/terminology cache download folder. */
    private String cacheDownloadFolderPath;

    public String getCacheDownloadFolderPath() {
        return cacheDownloadFolderPath;
    }

    public void setCacheDownloadFolderPath(String cacheDownloadFolderPath) {
        this.cacheDownloadFolderPath = cacheDownloadFolderPath;
    }

}
