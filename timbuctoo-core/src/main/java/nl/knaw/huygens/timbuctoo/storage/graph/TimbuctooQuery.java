package nl.knaw.huygens.timbuctoo.storage.graph;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.IsOfTypePredicate;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TimbuctooQuery {

  private Map<String, Object> hasProperties;
  private Class<? extends Entity> type;
  private IsOfTypePredicate isOfType = new IsOfTypePredicate();
  private PropertyBusinessRules businessRules;
  private Map<String, Field> fields;

  public TimbuctooQuery(Class<? extends Entity> type, PropertyBusinessRules businessRules) {
    this(type, businessRules, Maps.<String, Object> newHashMap());

  }

  TimbuctooQuery(Class<? extends Entity> type, PropertyBusinessRules businessRules, Map<String, Object> hasProperties) {
    this.hasProperties = hasProperties;
    this.type = type;
    this.fields = collectAllFields(type, businessRules);
    this.businessRules = businessRules;
  }

  @SuppressWarnings("unchecked")
  // collect the fields of the type and it's super types.
  private Map<String, Field> collectAllFields(Class<? extends Entity> type, PropertyBusinessRules businessRules) {
    Map<String, Field> fields = Maps.newHashMap();
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        fields.put(businessRules.getFieldName(type, field), field);
      }
    }
    return fields;
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  public TimbuctooQuery hasNotNullProperty(String name, Object value) {
    if (value != null) {
      hasProperties.put(name, value);
    }
    return this;
  }

  public TimbuctooQuery hasType(Class<? extends Entity> type) {
    this.type = type;
    return this;
  }

  public GraphQuery createGraphQuery(Graph db) {
    GraphQuery query = db.query();

    for (Entry<String, Object> entry : hasProperties.entrySet()) {
      query.has(getPropertyName(entry.getKey()), entry.getValue());
    }

    if (type != null) {
      query.has(ELEMENT_TYPES, isOfType, TypeNames.getInternalName(type));
    }

    return query;

  }

  private String getPropertyName(String name) {
    Field field = fields.get(name);

    String fieldName = businessRules.getFieldName(type, field);
    return businessRules.getFieldType(type, field).propertyName(type, fieldName);
  }

}
