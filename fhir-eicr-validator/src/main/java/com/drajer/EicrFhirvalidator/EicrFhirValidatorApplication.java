package com.drajer.EicrFhirvalidator;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r5.model.ImplementationGuide;
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

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
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
			Resource ecrRes = resourceLoader.getResource("classpath:ecrigs/package");
			Resource uscoreRes = resourceLoader.getResource("classpath:uscoreigs/package");
			Resource odhRes = resourceLoader.getResource("classpath:odhigs/package");
			Resource vrRes = resourceLoader.getResource("classpath:vrigs/package");
			List<ImplementationGuide> resList = new ArrayList<>();
			new ImplementationGuide().setUrl(res.getURI().getPath());
			resList.add(new ImplementationGuide().setUrl(res.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(ecrRes.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(uscoreRes.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(odhRes.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(vrRes.getURI().getPath()));
			return new Validator(resList);

		} catch (Exception e) {
			logger.error("There was an error initializing the validator:", e);
			e.printStackTrace();
			System.exit(1);
			return null; // unreachable
		}
	}
}
