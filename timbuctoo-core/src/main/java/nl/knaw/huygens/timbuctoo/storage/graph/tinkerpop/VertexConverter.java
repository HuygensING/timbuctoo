package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import com.tinkerpop.blueprints.Vertex;

public interface VertexConverter<T extends Entity> {

  void updateVertex(Vertex vertex, Entity entity) throws ConversionException;

  void updateModifiedAndRev(Vertex vertex, Entity entity) throws ConversionException;

  String getPropertyName(String fieldName);

  <U extends T> U convertToSubType(Class<U> type, Vertex vertex) throws ConversionException;

  T convertToEntity(Vertex vertex) throws ConversionException;

  void addValuesToVertex(Vertex vertex, T entity) throws ConversionException;

}
