package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexFields.VERTEX_TYPE;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Vertex;

class ExtendableVertexConverter<T extends Entity> implements VertexConverter<T> {

  private final EntityInstantiator entityInstantiator;
  private final Class<T> type;
  private Map<String, PropertyConverter> fieldNamePropertyConverterMap;

  ExtendableVertexConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
    this.type = type;
    this.entityInstantiator = entityInstantiator;
    mapPropertyConverters(propertyConverters);
  }

  private void mapPropertyConverters(Collection<PropertyConverter> propertyConverters) {
    fieldNamePropertyConverterMap = Maps.newHashMap();
    for (PropertyConverter propertyConverter : propertyConverters) {
      fieldNamePropertyConverterMap.put(propertyConverter.getFieldName(), propertyConverter);
    }
  }

  @Override
  public void addValuesToVertex(Vertex vertex, T entity) throws ConversionException {
    addVariation(vertex, entity.getClass());
    for (PropertyConverter propertyConverter : propertyConverters()) {
      propertyConverter.setPropertyOfVertex(vertex, entity);
    }
  }

  private void addVariation(Vertex vertex, Class<? extends Entity> variationType) {
    String[] types = (String[]) (vertex.getProperty(VERTEX_TYPE) != null ? vertex.getProperty(VERTEX_TYPE) : new String[] {});
    List<String> typeList = Lists.newArrayList(types);
    typeList.add(TypeNames.getInternalName(variationType));

    vertex.setProperty(VERTEX_TYPE, typeList.toArray(new String[typeList.size()]));
  }

  @Override
  public T convertToEntity(Vertex vertex) throws ConversionException {
    try {
      T entity = entityInstantiator.createInstanceOf(type);

      addValuesToEntity(vertex, entity);

      return entity;

    } catch (InstantiationException e) {
      throw new ConversionException("Entity could not be instantiated.");
    }

  }

  public <U extends T> U convertToSubType(Class<U> type, Vertex vertex) throws ConversionException {
    try {
      U entity = entityInstantiator.createInstanceOf(type);

      addValuesToEntity(vertex, entity);

      return entity;

    } catch (InstantiationException e) {
      throw new ConversionException(e);
    }
  }

  private <U extends T> void addValuesToEntity(Vertex vertex, U entity) throws ConversionException {
    for (PropertyConverter propertyConverter : propertyConverters()) {
      propertyConverter.addValueToEntity(entity, vertex);
    }
  }

  private Collection<PropertyConverter> propertyConverters() {
    return fieldNamePropertyConverterMap.values();
  }

  public String getPropertyName(String fieldName) {
    if (!hasPropertyContainerForField(fieldName)) {
      throw new FieldNonExistingException(type, fieldName);
    }
    return getPropertyConverterByFieldName(fieldName).propertyName();
  }

  private PropertyConverter getPropertyConverterByFieldName(String fieldName) {
    return fieldNamePropertyConverterMap.get(fieldName);
  }

  private boolean hasPropertyContainerForField(String fieldName) {
    return fieldNamePropertyConverterMap.containsKey(fieldName);
  }

  public void updateModifiedAndRev(Vertex vertexMock, Entity entity) throws ConversionException {
    getPropertyConverterByFieldName(MODIFIED_PROPERTY_NAME).setPropertyOfVertex(vertexMock, entity);
    getPropertyConverterByFieldName(REVISION_PROPERTY_NAME).setPropertyOfVertex(vertexMock, entity);
  }

  public void updateVertex(Vertex vertex, Entity entity) throws ConversionException {
    for (PropertyConverter propertyConverter : propertyConverters()) {

      if (propertyConverter.getFieldType() != FieldType.ADMINISTRATIVE) {
        propertyConverter.setPropertyOfVertex(vertex, entity);
      }
    }
  }
}