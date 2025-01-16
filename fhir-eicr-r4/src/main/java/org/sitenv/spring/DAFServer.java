package org.sitenv.spring;

import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;

/**
 * FHIR Server Servlet for handling FHIR R4 requests.
 * This servlet is responsible for hosting a FHIR server that processes RESTful
 * FHIR operations and serves as an entry point for FHIR clients.
 */
@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
public class DAFServer extends RestfulServer {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor that sets the FHIR context to R4 (FHIR Release 4).
     * Initializes the server with support for R4 resources and operations.
     */
    public DAFServer() {
        super(FhirContext.forR4());
    }

    /**
     * Initializes the server configuration, including resource providers and default settings.
     * This method is called automatically during servlet initialization.
     */
    @Override
    public void initialize() {
        // Register resource providers to handle FHIR resource types.
        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();

        resourceProviders.add(new PlanDefinitionResourceProvider());
        resourceProviders.add(new ValueSetResourceProvider());
        setProviders(new MessageHeaderResourceProvider());

        setResourceProviders(resourceProviders);

        // Enable pretty printing for better readability of JSON responses.
        setDefaultPrettyPrint(true);

        // Set the default response encoding format to JSON.
        setDefaultResponseEncoding(EncodingEnum.JSON);
    }
}
