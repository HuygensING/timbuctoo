package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;

import com.tinkerpop.blueprints.Vertex;

public class CompositeVertexConverter<T extends Entity> implements VertexConverter<T> {

  private Collection<VertexConverter<? super T>> delegates;

  public CompositeVertexConverter(Collection<VertexConverter<? super T>> delegates) {
    this.delegates = delegates;
  }

  @Override
  public void updateElement(Vertex vertex, Entity entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void updateModifiedAndRev(Vertex vertexMock, Entity entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public String getPropertyName(String fieldName) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <U extends T> U convertToSubType(Class<U> type, Vertex vertex) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public T convertToEntity(Vertex vertex) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void addValuesToElement(Vertex vertex, T entity) throws ConversionException {
    for (VertexConverter<? super T> vertexConverter : delegates) {
      vertexConverter.addValuesToElement(vertex, entity);
    }
  }

  int getNumberOfDelegates() {
    return delegates.size();
  }
}
