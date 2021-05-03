package com.drajer.EicrFhirvalidator;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.drajer.EicrFhirvalidator.component.Validator;

import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;

@SpringBootApplication
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
@ComponentScan(basePackages = { "com.drajer.EicrFhirvalidator" })
public class EicrFhirValidatorApplication extends SpringBootServletInitializer {

	private final Logger logger = LoggerFactory.getLogger(EicrFhirValidatorApplication.class);

	@Autowired
	private ResourceLoader resourceLoader;

	public static void main(String[] args) {
		SpringApplication.run(EicrFhirValidatorApplication.class, args);
		// initializeValidator();
	}

	@Bean
	public LoggingInterceptor loggingInterceptor() {
		return new LoggingInterceptor();
	}

	@PostConstruct
	public Validator initializeValidator() {
		logger.info("inside the initializeValidator");
		try {
			Resource res = resourceLoader.getResource("classpath:igs/package");
			return new Validator(res.getURI().getPath());
		} catch (Exception e) {
			logger.error("There was an error initializing the validator:", e);
			e.printStackTrace();
			System.exit(1);
			return null; // unreachable
		}
	}
}
