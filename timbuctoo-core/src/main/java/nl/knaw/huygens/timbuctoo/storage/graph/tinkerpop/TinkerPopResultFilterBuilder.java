package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.pipes.PipeFunction;

public class TinkerPopResultFilterBuilder {

  private Iterable<? extends String> fieldsWithDistinctValues;
  private final PropertyBusinessRules businessRules;
  private final PipeFunctionFactory pipeFunctionFactory;

  public TinkerPopResultFilterBuilder() {
    this(new PropertyBusinessRules(), new PipeFunctionFactory());
  }

  public TinkerPopResultFilterBuilder(PropertyBusinessRules businessRules, PipeFunctionFactory pipeFunctionFactory) {
    this.businessRules = businessRules;
    this.pipeFunctionFactory = pipeFunctionFactory;
  }

  public void setHasDistinctValues(Iterable<? extends String> fieldsWithDistinctValues) {
    this.fieldsWithDistinctValues = fieldsWithDistinctValues;
  }

  public TinkerPopResultFilter buildFor(Class<? extends Entity> type) {
    Map<String, Field> fields = collectAllFields(type);
    List<PipeFunction<Vertex, String>> pipeFunctions = Lists.newArrayList();

    for (String field : fieldsWithDistinctValues) {
      String propertyName = getPropertyName(fields, type, field);
      pipeFunctions.add(pipeFunctionFactory.forDistinctProperty(propertyName));
    }

    return new TinkerPopResultFilter(pipeFunctions);

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

    String fieldName = businessRules.getFieldName(type, field);
    return businessRules.getFieldType(type, field).propertyName(type, fieldName);
  }

}
