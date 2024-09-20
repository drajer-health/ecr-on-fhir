package com.drajer.eicrfhirvalidator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "eicr")
@ConditionalOnResource(resources = {"classpath:application.yml"})
public class EicrValidationProperties {

    private String cacheDownloadFolderPath;
   
    public String getCacheDownloadFolderPath() {
        return cacheDownloadFolderPath;
    }

    public void setCacheDownloadFolderPath(String cacheDownloadFolderPath) {
        this.cacheDownloadFolderPath = cacheDownloadFolderPath;
    }

}
