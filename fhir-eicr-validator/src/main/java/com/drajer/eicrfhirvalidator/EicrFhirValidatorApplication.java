package com.drajer.eicrfhirvalidator;


import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.util.Map;

/**
 * Spring Boot entry point for the EICR FHIR Validator service.
 *
 * <p>Registers a {@link com.drajer.eicrfhirvalidator.FhirRestfulServer} HAPI FHIR servlet under
 * {@code /fhir/*}, which exposes the {@code $validate} operation used to validate FHIR Bundles
 * (JSON or XML) against configured implementation guide profiles.
 */
@SpringBootApplication
public class EicrFhirValidatorApplication extends SpringBootServletInitializer {

    private static final String CACHE_FOLDER_PROPERTY = "eicr.cacheDownloadFolderPath";
    private static final String CACHE_FOLDER_NAME = "fhir-package-cache";

    @Autowired
    private ApplicationContext context;

    /**
     * Application entry point; boots the embedded servlet container via Spring Boot.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(EicrFhirValidatorApplication.class);
        // Embedded Tomcat points catalina.base at a throwaway temp folder, so standalone runs
        // keep the package cache under the user's home instead
        application.setDefaultProperties(Map.of(CACHE_FOLDER_PROPERTY,
                Path.of(System.getProperty("user.home"), CACHE_FOLDER_NAME).toString()));
        application.run(args);
    }

    /**
     * Entry point when deployed as a WAR to an external Tomcat. Defaults the package cache to
     * {@code <catalina.base>/fhir-package-cache}, which survives restarts and redeploys.
     *
     * @param builder the application builder supplied by {@link SpringBootServletInitializer}
     * @return the configured builder
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        String root = System.getProperty("catalina.base", System.getProperty("user.home"));
        // properties() are defaults: env vars, -D flags and external config still override them
        return builder.sources(EicrFhirValidatorApplication.class)
                .properties(CACHE_FOLDER_PROPERTY + "=" + Path.of(root, CACHE_FOLDER_NAME));
    }

    /**
     * Registers the HAPI {@link com.drajer.eicrfhirvalidator.FhirRestfulServer} servlet under the
     * {@code /fhir/*} path mapping.
     *
     * @return the servlet registration for the FHIR REST endpoint
     */
    @Bean
    public ServletRegistrationBean ServletRegistrationBean() {
        FhirContext r4FhirContext = context.getBean("r4FhirContext", FhirContext.class);
        ServletRegistrationBean registration= new ServletRegistrationBean(new FhirRestfulServer(context, r4FhirContext),"/fhir/*");
        registration.setName("FhirServlet");
        return registration;
    }
}
