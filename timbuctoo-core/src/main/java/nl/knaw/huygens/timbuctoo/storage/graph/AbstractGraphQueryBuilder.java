package nl.knaw.huygens.timbuctoo.storage.graph;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Maps;

public class AbstractGraphQueryBuilder {

  protected final PropertyBusinessRules businessRules;
  protected final Map<String, Field> fields;
  protected Class<? extends Entity> type;
  protected Map<String, Object> hasProperties;

  public AbstractGraphQueryBuilder(Class<? extends Entity> type, PropertyBusinessRules businessRules) {
    this.type = type;
    this.businessRules = businessRules;
    this.fields = collectAllFields(type);
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Field> collectAllFields(Class<? extends Entity> type) {
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

  protected String getPropertyName(String name) {
    Field field = fields.get(name);

    String fieldName = businessRules.getFieldName(type, field);
    return businessRules.getFieldType(type, field).propertyName(type, fieldName);
  }

  public void setHasProperties(Map<String, Object> hasProperties) {
    this.hasProperties = hasProperties;
  }

  public void setType(Class<? extends Entity> type) {
    this.type = type;
  }

}