package org.sitenv.spring.dao;

import java.util.List;
import org.sitenv.spring.model.DafPlanDefinition;
import org.sitenv.spring.util.SearchParameterMap;

public interface PlanDefinitionDao {
  DafPlanDefinition getPlanDefinitionById(String paramString);
  
  List<DafPlanDefinition> getAllPlanDefinitions();
  
  void createPlanDefinition(DafPlanDefinition paramDafPlanDefinition);
  
  void updatePlanDefinition(DafPlanDefinition paramDafPlanDefinition);
  
  List<DafPlanDefinition> search(SearchParameterMap paramSearchParameterMap);
}
