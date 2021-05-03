package org.sitenv.spring;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.sitenv.spring.configuration.AppConfig;
import org.sitenv.spring.model.DafPlanDefinition;
import org.sitenv.spring.service.PlanDefinitionService;
import org.sitenv.spring.util.SearchParameterMap;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

public class PlanDefinitionResourceProvider implements IResourceProvider {
	private static final FhirContext fhirContext = FhirContext.forR4();

	AbstractApplicationContext context;
	PlanDefinitionService service;

	public PlanDefinitionResourceProvider() {
		context = new AnnotationConfigApplicationContext(AppConfig.class);
		service = (PlanDefinitionService) context.getBean("PlanDefinitionService");
	}

	public static final String RESOURCE_TYPE = "PlanDefinition";

	public static final String VERSION_ID = "1.0";

	public Class<? extends IBaseResource> getResourceType() {
		return (Class) PlanDefinition.class;
	}

	@Read
	public PlanDefinition readOrVread(@IdParam IdType theId) {
		String id;
		try {
			id = theId.getIdPart();
		} catch (NumberFormatException e) {
			throw new ResourceNotFoundException(theId);
		}
		DafPlanDefinition dafPlanDefinition = this.service.getPlanDefinitionById(id);
		return (PlanDefinition) fhirContext.newJsonParser().parseResource(dafPlanDefinition.getData());
	}

	@Create
	public MethodOutcome createPlanDefinition(@ResourceParam PlanDefinition planDefinition) {
		String uuid = getUUID();
		planDefinition.setId(uuid);
		Meta meta = new Meta();
		meta.setLastUpdated(new Date());
		meta.setVersionId("1");
		planDefinition.setMeta(meta);
		DafPlanDefinition dafPlanDefinition = new DafPlanDefinition();
		dafPlanDefinition.setData(fhirContext.newJsonParser().encodeResourceToString((IBaseResource) planDefinition));
		dafPlanDefinition.setTimestamp(new Date());
		this.service.createPlanDefinition(dafPlanDefinition);
		MethodOutcome retVal = new MethodOutcome();
		retVal.setId((IIdType) new IdType("PlanDefinition", uuid, "1"));
		return retVal;
	}

	@Update
	public MethodOutcome updatePlanDefinition(@ResourceParam PlanDefinition planDefinition, @IdParam IdType theId) {
		String resourceId = theId.getIdPart();
		String versionNum = null;
		DafPlanDefinition dafPlanDefinition = this.service.getPlanDefinitionById(resourceId);
		if (dafPlanDefinition != null) {
			PlanDefinition existingPlanDefinition = (PlanDefinition) fhirContext.newJsonParser()
					.parseResource(dafPlanDefinition.getData());
			Integer existingResourceVersion = Integer
					.valueOf(Integer.parseInt(existingPlanDefinition.getMeta().getVersionId()));
			Meta updateMeta = existingPlanDefinition.getMeta();
			versionNum = String.valueOf(existingResourceVersion.intValue() + 1);
			updateMeta.setVersionId(versionNum);
			updateMeta.setLastUpdated(new Date());
			planDefinition.setMeta(updateMeta);
			dafPlanDefinition
					.setData(fhirContext.newJsonParser().encodeResourceToString((IBaseResource) planDefinition));
			this.service.updatePlanDefinition(dafPlanDefinition);
			MethodOutcome retVal = new MethodOutcome();
			retVal.setId((IIdType) new IdType("PlanDefinition", resourceId, versionNum));
			return retVal;
		}
		throw new ResourceNotFoundException(planDefinition.getId());
	}

	@Search
	public IBundleProvider search(HttpServletRequest theServletRequest,
			@Description(shortDefinition = "The resource identity") @OptionalParam(name = "_id") StringAndListParam theId,
			@Description(shortDefinition = "A PlanDefinition identifier") @OptionalParam(name = "identifier") TokenAndListParam theIdentifier,
			@Description(shortDefinition = "Name for this plan definition (computer friendly)") @OptionalParam(name = "name") StringAndListParam theName,
			@Description(shortDefinition = "Name for this plan definition (human friendly)") @OptionalParam(name = "title") StringAndListParam theTitle,
			@Description(shortDefinition = "Name of the publisher (organization or individual)") @OptionalParam(name = "publisher") StringAndListParam thePublisher,
			@IncludeParam(reverse = true, allow = { "*" }) Set<Include> theRevIncludes,
			@IncludeParam(allow = { "*" }) Set<Include> theIncludes, @Sort SortSpec theSort, @Count Integer theCount) {
		SearchParameterMap paramMap = new SearchParameterMap();
		paramMap.add(PlanDefinition.SP_RES_ID, theId);
		paramMap.add(PlanDefinition.SP_IDENTIFIER, theIdentifier);
		paramMap.add(PlanDefinition.SP_NAME, theName);
		paramMap.add(PlanDefinition.SP_TITLE, theTitle);
		paramMap.add(PlanDefinition.SP_PUBLISHER, thePublisher);
		paramMap.setIncludes(theIncludes);
		paramMap.setSort(theSort);
		paramMap.setCount(theCount);
		final List<DafPlanDefinition> results = this.service.search(paramMap);
		return new IBundleProvider() {
			final InstantDt published = InstantDt.withCurrentTime();

			public List<IBaseResource> getResources(int theFromIndex, int theToIndex) {
				List<IBaseResource> planDefinitionList = new ArrayList<>();
				List<String> ids = new ArrayList<>();
				for (DafPlanDefinition dafPlanDefinition : results) {
					PlanDefinition PlanDefinition = (PlanDefinition) PlanDefinitionResourceProvider.fhirContext
							.newJsonParser().parseResource(dafPlanDefinition.getData());
					planDefinitionList.add(PlanDefinition);
					ids.add(PlanDefinition.getIdElement().getResourceType() + "/"
							+ PlanDefinition.getIdElement().getIdPart());
				}
				return planDefinitionList;
			}

			public Integer size() {
				return Integer.valueOf(results.size());
			}

			public InstantDt getPublished() {
				return this.published;
			}

			public Integer preferredPageSize() {
				return null;
			}

			public String getUuid() {
				return null;
			}
		};
	}

	public String getUUID() {
		UUID uuid = UUID.randomUUID();
		String randomUUID = uuid.toString();
		return randomUUID;
	}
}
