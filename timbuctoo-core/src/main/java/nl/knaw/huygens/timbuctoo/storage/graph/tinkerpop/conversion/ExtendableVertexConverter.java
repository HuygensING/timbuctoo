package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;

import com.tinkerpop.blueprints.Vertex;

class ExtendableVertexConverter<T extends Entity> extends AbstractExtendableConverter<T, Vertex> implements VertexConverter<T> {
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
}