package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import com.tinkerpop.blueprints.Edge;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;

import java.util.List;

public class CompositeEdgeConverter<T extends Relation> implements EdgeConverter<T> {

  private List<EdgeConverter<? super T>> delegates;

  public CompositeEdgeConverter(List<EdgeConverter<? super T>> delegates) {
    this.delegates = delegates;
  }

  @Override
  public void updateElement(Edge edge, Entity entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void updateModifiedAndRev(Edge edge, Entity entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public String getPropertyName(String fieldName) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public T convertToEntity(Edge edge) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void addValuesToElement(Edge edge, T entity) throws ConversionException {
    for (EdgeConverter<? super T> edgeConverter : delegates) {
      edgeConverter.addValuesToElement(edge, entity);
    }
  }

  int getNumberOfDelegates() {
    return delegates.size();
  }

  @Override
  public void removePropertyByFieldName(Edge edge, String fieldName) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <U extends T> U convertToSubType(Class<U> type, Edge element) throws ConversionException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
