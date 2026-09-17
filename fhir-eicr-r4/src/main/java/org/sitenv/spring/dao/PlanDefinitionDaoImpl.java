package org.sitenv.spring.dao;

import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.query.NativeQuery;
import org.sitenv.spring.model.DafPlanDefinition;
import org.sitenv.spring.util.SearchParameterMap;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hibernate-backed implementation of {@link PlanDefinitionDao}. Search
 * filtering is built as parameterized native SQL against the JSONB
 * {@code data} column of the {@code plandefinition} table, since Hibernate's
 * Criteria API (used prior to Hibernate 6) has no direct replacement for
 * querying JSON path expressions.
 */
@Repository("PlanDefinitionDao")
public class PlanDefinitionDaoImpl extends AbstractDao implements PlanDefinitionDao {

  public DafPlanDefinition getPlanDefinitionById(String id) {
    NativeQuery<DafPlanDefinition> query = getSession().createNativeQuery(
        "select * from plandefinition where data->>'id' = :id order by data->'meta'->>'versionId' desc",
        DafPlanDefinition.class);
    query.setParameter("id", id);
    return query.list().get(0);
  }

  public List<DafPlanDefinition> getAllPlanDefinitions() {
    NativeQuery<DafPlanDefinition> query = getSession().createNativeQuery(
        "select * from plandefinition order by data->'meta'->>'versionId' desc",
        DafPlanDefinition.class);
    return query.list();
  }

  public void createPlanDefinition(DafPlanDefinition dafPlanDefinition) {
    getSession().saveOrUpdate(dafPlanDefinition);
  }

  public void updatePlanDefinition(DafPlanDefinition dafPlanDefinition) {
    getSession().saveOrUpdate(dafPlanDefinition);
  }

  /**
   * Builds and runs a single native query ANDing together whichever of the
   * id / identifier / name / title / publisher filters are present in
   * {@code theMap}. Filter values are always passed as bind parameters,
   * never concatenated into the SQL text.
   */
  public List<DafPlanDefinition> search(SearchParameterMap theMap) {
    StringBuilder where = new StringBuilder();
    Map<String, Object> params = new LinkedHashMap<>();
    AtomicInteger paramSeq = new AtomicInteger();

    buildIdCriteria(theMap, where, params, paramSeq);
    buildIdentifierCriteria(theMap, where, params, paramSeq);
    buildNameCriteria(theMap, where, params, paramSeq);
    buildTitleCriteria(theMap, where, params, paramSeq);
    buildPublisherCriteria(theMap, where, params, paramSeq);

    String sql = "select * from plandefinition" + (where.length() > 0 ? " where " + where : "");
    NativeQuery<DafPlanDefinition> query = getSession().createNativeQuery(sql, DafPlanDefinition.class);
    params.forEach(query::setParameter);
    return query.list();
  }

  /**
   * Appends {@code condition} to the WHERE clause under construction,
   * ANDing it with whatever conditions were already appended.
   *
   * @param where the WHERE clause being built
   * @param condition the SQL condition fragment to append
   */
  private void addAndCondition(StringBuilder where, String condition) {
    if (where.length() > 0) {
      where.append(" and ");
    }
    where.append(condition);
  }

  /**
   * Adds an equality condition on {@code data->>'id'} for each {@code _id} value in the search map.
   */
  private void buildIdCriteria(SearchParameterMap theMap, StringBuilder where, Map<String, Object> params, AtomicInteger paramSeq) {
    List<List<? extends IQueryParameterType>> list = theMap.get("_id");
    if (list != null)
      for (List<? extends IQueryParameterType> values : list) {
        for (IQueryParameterType param : values) {
          StringParam id = (StringParam) param;
          if (id.getValue() != null) {
            String name = "p" + paramSeq.incrementAndGet();
            addAndCondition(where, "data->>'id' = :" + name);
            params.put(name, id.getValue());
          }
        }
      }
  }

  /**
   * Adds an OR-group per {@code identifier} value in the search map, matching
   * against either of the first two {@code identifier} entries' {@code value}
   * or {@code system} fields (case-insensitive, substring match).
   */
  private void buildIdentifierCriteria(SearchParameterMap theMap, StringBuilder where, Map<String, Object> params, AtomicInteger paramSeq) {
    List<List<? extends IQueryParameterType>> list = theMap.get("identifier");
    if (list != null)
      for (List<? extends IQueryParameterType> values : list) {
        StringBuilder group = new StringBuilder();
        for (IQueryParameterType param : values) {
          TokenParam identifier = (TokenParam) param;
          if (identifier.getValue() != null) {
            String name = "p" + paramSeq.incrementAndGet();
            params.put(name, "%" + identifier.getValue() + "%");
            String condition = "(data->'identifier'->0->>'value' ilike :" + name
                + " or data->'identifier'->1->>'value' ilike :" + name
                + " or data->'identifier'->0->>'system' ilike :" + name
                + " or data->'identifier'->1->>'system' ilike :" + name + ")";
            if (group.length() > 0) {
              group.append(" or ");
            }
            group.append(condition);
          }
        }
        if (group.length() > 0) {
          addAndCondition(where, "(" + group + ")");
        }
      }
  }

  /**
   * Adds an equality condition on {@code data->>'name'} for each {@code name} value in the search map.
   */
  private void buildNameCriteria(SearchParameterMap theMap, StringBuilder where, Map<String, Object> params, AtomicInteger paramSeq) {
    List<List<? extends IQueryParameterType>> list = theMap.get("name");
    if (list != null)
      for (List<? extends IQueryParameterType> values : list) {
        for (IQueryParameterType param : values) {
          StringParam name = (StringParam) param;
          if (name.getValue() != null) {
            String paramName = "p" + paramSeq.incrementAndGet();
            addAndCondition(where, "data->>'name' = :" + paramName);
            params.put(paramName, name.getValue());
          }
        }
      }
  }

  /**
   * Adds an equality condition on {@code data->>'title'} for each {@code title} value in the search map.
   */
  private void buildTitleCriteria(SearchParameterMap theMap, StringBuilder where, Map<String, Object> params, AtomicInteger paramSeq) {
    List<List<? extends IQueryParameterType>> list = theMap.get("title");
    if (list != null)
      for (List<? extends IQueryParameterType> values : list) {
        for (IQueryParameterType param : values) {
          StringParam title = (StringParam) param;
          if (title.getValue() != null) {
            String paramName = "p" + paramSeq.incrementAndGet();
            addAndCondition(where, "data->>'title' = :" + paramName);
            params.put(paramName, title.getValue());
          }
        }
      }
  }

  /**
   * Adds an equality condition on {@code data->>'publisher'} for each {@code publisher} value in the search map.
   */
  private void buildPublisherCriteria(SearchParameterMap theMap, StringBuilder where, Map<String, Object> params, AtomicInteger paramSeq) {
    List<List<? extends IQueryParameterType>> list = theMap.get("publisher");
    if (list != null)
      for (List<? extends IQueryParameterType> values : list) {
        for (IQueryParameterType param : values) {
          StringParam publisher = (StringParam) param;
          if (publisher.getValue() != null) {
            String paramName = "p" + paramSeq.incrementAndGet();
            addAndCondition(where, "data->>'publisher' = :" + paramName);
            params.put(paramName, publisher.getValue());
          }
        }
      }
  }
}
