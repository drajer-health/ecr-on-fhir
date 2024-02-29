package com.drajer.EicrFhirvalidator;

import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import com.drajer.EicrFhirvalidator.component.Validator;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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

	@PostConstruct
	public Validator initializeValidator() {
		logger.info("inside the initializeValidator");
		try {
//			Resource igRes = resourceLoader.getResource("classpath:igs/package");

			Resource usCore311 = resourceLoader.getResource("classpath:hl7.fhir.us.core-3.1.1/package");
			Resource usCore400 = resourceLoader.getResource("classpath:hl7.fhir.us.core-4.0.0/package");
			Resource usecr211 = resourceLoader.getResource("classpath:hl7.fhir.us.ecr-2.1.1/package");
			Resource medmorph020 = resourceLoader.getResource("classpath:hl7.fhir.us.medmorph-0.2.0/package");
			Resource odh110 = resourceLoader.getResource("classpath:hl7.fhir.us.odh-1.1.0/package");
			Resource commonLibrary100 = resourceLoader.getResource("classpath:hl7.fhir.us.vr-common-library-1.0.0/package");
			Resource bulkdata100 = resourceLoader.getResource("classpath:hl7.fhir.uv.bulkdata-1.0.0/package");
			Resource bulkdata101 = resourceLoader.getResource("classpath:hl7.fhir.uv.bulkdata-1.0.1/package");
			Resource bulkdata110 = resourceLoader.getResource("classpath:hl7.fhir.uv.bulkdata-1.1.0/package");
			Resource backport010 = resourceLoader.getResource("classpath:hl7.fhir.uv.subscriptions-backport-0.1.0/package");
			Resource extensions004 = resourceLoader.getResource("classpath:hl7.fhir.xver-extensions-0.0.4/package");
			Resource terminology431 = resourceLoader.getResource("classpath:hl7.terminology.r4-3.1.0/package");
			Resource phinvads070 = resourceLoader.getResource("classpath:us.cdc.phinvads-0.7.0/package");
			Resource phinvads010 = resourceLoader.getResource("classpath:us.cdc.phinvads-0.10.0/package");
			Resource vsac030 = resourceLoader.getResource("classpath:us.nlm.vsac-0.3.0/package");
			Resource vsac070 = resourceLoader.getResource("classpath:us.nlm.vsac-0.7.0/package");


			List<ImplementationGuide> resList = new ArrayList<>();
			resList.add(new ImplementationGuide().setUrl(usCore311.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(usCore400.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(usecr211.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(medmorph020.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(odh110.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(commonLibrary100.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(bulkdata100.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(bulkdata101.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(bulkdata110.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(backport010.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(extensions004.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(terminology431.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(phinvads070.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(phinvads010.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(vsac030.getURI().getPath()));
			resList.add(new ImplementationGuide().setUrl(vsac070.getURI().getPath()));

			return new Validator(resList);

		} catch (Exception e) {
			logger.error("There was an error initializing the validator:", e);
			e.printStackTrace();
			System.exit(1);
			return null; // unreachable
		}
	}
}
