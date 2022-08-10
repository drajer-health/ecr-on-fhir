package com.drajer.EicrFhirvalidator;

import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class EicrFhirValidatorApplication extends SpringBootServletInitializer {

	private final Logger logger = LoggerFactory.getLogger(EicrFhirValidatorApplication.class);

	@Autowired
	private ResourceLoader resourceLoader;

	public static void main(String[] args) {
		SpringApplication.run(EicrFhirValidatorApplication.class, args);
	}

	@Bean
	public LoggingInterceptor loggingInterceptor() {
		return new LoggingInterceptor();
	}

}
