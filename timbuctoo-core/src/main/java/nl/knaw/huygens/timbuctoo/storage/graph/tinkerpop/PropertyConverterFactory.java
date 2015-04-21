package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

public class PropertyConverterFactory {

  public <T extends Entity> void addPropertyConverter(VertexConverter<T> vertexConveter, Field field) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  public <T extends Entity> PropertyConverter createPropertyConverter(Class<T> type, Field any) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
