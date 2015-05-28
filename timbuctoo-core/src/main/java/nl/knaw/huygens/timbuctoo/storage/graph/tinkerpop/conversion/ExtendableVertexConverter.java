package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;

import com.tinkerpop.blueprints.Vertex;

class ExtendableVertexConverter<T extends Entity> extends AbstractExtendableElementConverter<T, Vertex> implements VertexConverter<T> {
  ExtendableVertexConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
    super(type, propertyConverters, entityInstantiator);
  }

  @Override
  public <U extends T> U convertToSubType(Class<U> type, Vertex vertex) throws ConversionException {
    try {
      U entity = entityInstantiator.createInstanceOf(type);

      addValuesToEntity(entity, vertex);

      return entity;

    } catch (InstantiationException e) {
      throw new ConversionException(e);
    }
  }

  @Override
  protected void executeCustomDeserializationActions(T entity, Vertex element) {
    // nothing to do
  }

  @Override
  public void removeVariant(Vertex vertex) {
    removeVariation(vertex);
    removeProperties(vertex);
  }

  private void removeVariation(Vertex vertex) {
    List<String> types = getTypesProperty(vertex);

    types.remove(TypeNames.getInternalName(type));

    setTypesProperty(vertex, types);
  }

  private void removeProperties(Vertex vertex) {
    for (PropertyConverter propertyConverter : propertyConverters()) {
      if (propertyConverter.getFieldType() != FieldType.ADMINISTRATIVE) {
        propertyConverter.removeFrom(vertex);
      }
    }
  }

  @Override
  public void removePropertyByFieldName(Vertex vertex, String fieldName) {
    if (!hasPropertyConverterForField(fieldName)) {
      throw new NoSuchFieldException(type, fieldName);
    }

    getPropertyConverterByFieldName(fieldName).removeFrom(vertex);
  }
}