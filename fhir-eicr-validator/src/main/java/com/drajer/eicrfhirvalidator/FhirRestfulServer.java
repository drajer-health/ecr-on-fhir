package com.drajer.eicrfhirvalidator;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import com.drajer.eicrfhirvalidator.providers.BundleResourceProvider;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;


@RequestMapping("/fhir/*")
public class FhirRestfulServer extends RestfulServer {

    private final ApplicationContext applicationContext;
    @Autowired
    FhirRestfulServer(ApplicationContext context) {
        this.applicationContext = context;
    }

    @Override
    protected void initialize() throws ServletException{
        super.initialize();
        setFhirContext(FhirContext.forR4());
        setResourceProviders(Arrays.asList(
                applicationContext.getBean(BundleResourceProvider.class)));
    }
}
