package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import com.tinkerpop.blueprints.Vertex;

public interface VertexConverter<T extends Entity> extends ElementConverter<T, Vertex> {

  <U extends T> U convertToSubType(Class<U> type, Vertex vertex) throws ConversionException;

  void removeVariant(Vertex vertex);

  void removePropertyFromVertexByFieldName(Vertex vertex, String fieldName);

}
