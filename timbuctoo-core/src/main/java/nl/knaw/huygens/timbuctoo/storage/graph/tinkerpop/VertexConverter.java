package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import com.tinkerpop.blueprints.Vertex;

public interface VertexConverter<T extends Entity> {

  void addValuesToVertex(Vertex vertex, T entity) throws ConversionException;

  T convertToEntity(Vertex foundVertex) throws ConversionException;

}
