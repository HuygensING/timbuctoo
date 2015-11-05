package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.ResultFilter;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class TinkerPopResultFilter<T extends Element> implements ResultFilter {

  private final PipeFunctionFactory pipeFunctionFactory;
  private final PropertyBusinessRules businessRules;
  private Set<String> disitinctFields;
  private Class<? extends Entity> type;

  public TinkerPopResultFilter(PipeFunctionFactory pipeFunctionFactory, PropertyBusinessRules businessRules) {
    this.pipeFunctionFactory = pipeFunctionFactory;
    this.businessRules = businessRules;
  }

  public Iterable<T> filter(Iterable<T> iterableToFilter) {
    GremlinPipeline<Iterable<T>, T> pipeline = createPipeline(iterableToFilter);

    Map<String, Field> fields = collectAllFields(type);

    for (String distinctField : disitinctFields) {
      String propertyName = getPropertyName(fields, type, distinctField);

      pipeline.dedup(pipeFunctionFactory.<T, Object> forDistinctProperty(propertyName));
    }

    return pipeline.toList();
  }

  GremlinPipeline<Iterable<T>, T> createPipeline(Iterable<T> iterableToFilter) {
    return new GremlinPipeline<Iterable<T>, T>(iterableToFilter);
  }

  @Override
  public void setDistinctFields(Set<String> disitinctFields) {
    this.disitinctFields = disitinctFields;
  }

  @Override
  public void setType(Class<? extends Entity> type) {
    this.type = type;
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

  protected String getPropertyName(Map<String, Field> fields, Class<? extends Entity> type, String name) {
    Field field = fields.get(name);

    if (field == null) {
      throw new NoSuchFieldException(type, name);
    }

    String propertyName = businessRules.getPropertyName(type, field);
    return businessRules.getFieldType(type, field).completePropertyName(type, propertyName);
  }

}
