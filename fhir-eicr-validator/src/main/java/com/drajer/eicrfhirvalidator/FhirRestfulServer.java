package com.drajer.eicrfhirvalidator;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import com.drajer.eicrfhirvalidator.providers.BundleResourceProvider;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;


/**
 * HAPI FHIR {@link RestfulServer} exposing the R4 {@code Bundle} resource provider under
 * {@code /fhir/*}. Request/response format negotiation (JSON vs XML) is handled by HAPI's
 * default parser selection based on the {@code Content-Type}/{@code Accept} headers; no
 * additional configuration is applied here.
 */
@RequestMapping("/fhir/*")
public class FhirRestfulServer extends RestfulServer {

    private final ApplicationContext applicationContext;

    /**
     * @param context the Spring application context used to look up resource provider beans
     */
    @Autowired
    FhirRestfulServer(ApplicationContext context) {
        this.applicationContext = context;
    }

    /**
     * Configures the FHIR context (R4) and registers the {@link BundleResourceProvider} as the
     * server's resource provider.
     *
     * @throws ServletException if servlet initialization fails
     */
    @Override
    protected void initialize() throws ServletException{
        super.initialize();
        setFhirContext(FhirContext.forR4());
        setResourceProviders(Arrays.asList(
                applicationContext.getBean(BundleResourceProvider.class)));
    }
}
