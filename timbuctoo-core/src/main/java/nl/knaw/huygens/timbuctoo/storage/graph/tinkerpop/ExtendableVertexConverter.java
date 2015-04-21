package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import com.tinkerpop.blueprints.Vertex;

class ExtendableVertexConverter<T extends Entity> implements VertexConverter<T> {

  ExtendableVertexConverter(Collection<PropertyConverter> propertyConverters) {}

  @Override
  public void addValuesToVertex(Vertex vertex, T entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  void addPropertyContainer(PropertyConverter createPropertyConverter) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}