package com.drajer.eicrfhirvalidator;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EicrFhirValidatorApplication extends SpringBootServletInitializer {

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {

        SpringApplication.run(EicrFhirValidatorApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean ServletRegistrationBean() {
        ServletRegistrationBean registration= new ServletRegistrationBean(new FhirRestfulServer(context),"/fhir/*");
        registration.setName("FhirServlet");
        return registration;
    }
}
