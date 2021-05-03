package org.sitenv.spring;

import java.util.Date;
import java.util.UUID;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.ValueSet;
import org.sitenv.spring.configuration.AppConfig;
import org.sitenv.spring.model.DafValueSet;
import org.sitenv.spring.service.ValueSetService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class ValueSetResourceProvider implements IResourceProvider{

	public static final String RESOURCE_TYPE = "ValueSet";
    public static final String VERSION_ID = "1.0";
    AbstractApplicationContext context;
    ValueSetService service;
    
    private static final FhirContext fhirContext = FhirContext.forR4();

    public ValueSetResourceProvider() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        service = (ValueSetService) context.getBean("ValueSetService");
    }

    /**
     * The getResourceType method comes from IResourceProvider, and must
     * be overridden to indicate what type of resource this provider
     * supplies.
     */
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ValueSet.class;
	}
	
	@Read
	public ValueSet readOrVread(@IdParam IdType theId) {
		String id;
		try {
			id = theId.getIdPart();
		} catch (NumberFormatException e) {
			throw new ResourceNotFoundException(theId);
		}
		DafValueSet dafValueSet = this.service.getValueSetById(id);
		return (ValueSet) fhirContext.newJsonParser().parseResource(dafValueSet.getData());
	}

	@Create
	public MethodOutcome createValueSet(@ResourceParam ValueSet ValueSet) {
		String uuid = getUUID();
		ValueSet.setId(uuid);
		Meta meta = new Meta();
		meta.setLastUpdated(new Date());
		meta.setVersionId("1");
		ValueSet.setMeta(meta);
		DafValueSet dafValueSet = new DafValueSet();
		dafValueSet.setData(fhirContext.newJsonParser().encodeResourceToString((IBaseResource) ValueSet));
		dafValueSet.setTimestamp(new Date());
		this.service.createValueSets(dafValueSet);
		MethodOutcome retVal = new MethodOutcome();
		retVal.setId((IIdType) new IdType("ValueSet", uuid, "1"));
		return retVal;
	}
	
	public String getUUID() {
		UUID uuid = UUID.randomUUID();
		String randomUUID = uuid.toString();
		return randomUUID;
	}
}
