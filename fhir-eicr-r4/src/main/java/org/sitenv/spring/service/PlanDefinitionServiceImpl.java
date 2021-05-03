package org.sitenv.spring.service;

import java.util.List;
import org.sitenv.spring.dao.PlanDefinitionDao;
import org.sitenv.spring.model.DafPlanDefinition;
import org.sitenv.spring.util.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("PlanDefinitionService")
@Transactional
public class PlanDefinitionServiceImpl implements PlanDefinitionService {
  @Autowired
  private PlanDefinitionDao planDefinitionDao;
  
  public DafPlanDefinition getPlanDefinitionById(String id) {
    return this.planDefinitionDao.getPlanDefinitionById(id);
  }
  
  public List<DafPlanDefinition> getAllPlanDefinitions() {
    return this.planDefinitionDao.getAllPlanDefinitions();
  }
  
  public void createPlanDefinition(DafPlanDefinition dafPlanDefinition) {
    this.planDefinitionDao.createPlanDefinition(dafPlanDefinition);
  }
  
  public void updatePlanDefinition(DafPlanDefinition dafPlanDefinition) {
    this.planDefinitionDao.updatePlanDefinition(dafPlanDefinition);
  }
  
  public List<DafPlanDefinition> search(SearchParameterMap theMap) {
    return this.planDefinitionDao.search(theMap);
  }
}
