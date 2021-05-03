package org.sitenv.spring;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;


@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
public class DAFServer extends RestfulServer {


    /**
     * Constructor
     */
    public DAFServer() {
        super(FhirContext.forR4()); // Support R4
    }

    /**
     * This method is called automatically when the
     * servlet is initializing.
     */
    @Override
    public void initialize() {

      /*
       * The servlet defines any number of resource providers, and
       * configures itself to use them by calling
       * setResourceProviders()
       */

        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
      
        resourceProviders.add(new PlanDefinitionResourceProvider());
        resourceProviders.add(new ValueSetResourceProvider());
        setProviders(new MessageHeaderResourceProvider());

        setResourceProviders(resourceProviders);


		/*
         * Use a narrative generator. This is a completely optional step,
		 * but can be useful as it causes HAPI to generate narratives for
		 * resources which don't otherwise have one.
		 */
        /** -- revisit Devil */
       /* INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);*/

		/*
         * Tells HAPI to use content types which are not technically FHIR compliant when a browser is detected as the
		 * requesting client. This prevents browsers from trying to download resource responses instead of displaying them
		 * inline which can be handy for troubleshooting.
		 */
        //setUseBrowserFriendlyContentTypes(true);
        setDefaultPrettyPrint(true);
        setDefaultResponseEncoding(EncodingEnum.JSON);

    }

}
