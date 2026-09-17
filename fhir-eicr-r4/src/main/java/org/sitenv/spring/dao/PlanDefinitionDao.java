package org.sitenv.spring.dao;

import java.util.List;
import org.sitenv.spring.model.DafPlanDefinition;
import org.sitenv.spring.util.SearchParameterMap;

/**
 * Persistence contract for {@link DafPlanDefinition} records.
 */
public interface PlanDefinitionDao {

  /**
   * @param paramString the FHIR logical id of the PlanDefinition resource
   * @return the matching plan definition record (highest version first)
   */
  DafPlanDefinition getPlanDefinitionById(String paramString);

  /**
   * @return all plan definition records, most recently versioned first
   */
  List<DafPlanDefinition> getAllPlanDefinitions();

  /**
   * Saves a new plan definition record.
   *
   * @param paramDafPlanDefinition the plan definition to persist
   */
  void createPlanDefinition(DafPlanDefinition paramDafPlanDefinition);

  /**
   * Updates an existing plan definition record.
   *
   * @param paramDafPlanDefinition the plan definition to persist
   */
  void updatePlanDefinition(DafPlanDefinition paramDafPlanDefinition);

  /**
   * Searches plan definitions by any combination of id, identifier, name,
   * title, and publisher, as populated in the given search map.
   *
   * @param paramSearchParameterMap the FHIR search parameters, keyed by parameter name
   * @return the matching plan definition records
   */
  List<DafPlanDefinition> search(SearchParameterMap paramSearchParameterMap);
}
