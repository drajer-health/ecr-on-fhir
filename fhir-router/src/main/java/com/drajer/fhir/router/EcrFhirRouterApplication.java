package com.drajer.fhir.router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
//@EnableScheduling
public class EcrFhirRouterApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcrFhirRouterApplication.class, args);
		
	}
	
//	@Scheduled(fixedDelay = 10000)
//	public void run() {
//		System.out.println("Running");
//	}	
}
