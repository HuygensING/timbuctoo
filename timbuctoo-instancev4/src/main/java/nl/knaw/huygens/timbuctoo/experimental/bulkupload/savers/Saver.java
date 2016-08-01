package nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

public interface Saver {
  Vertex addEntity(Vertex collection, Map<String, Object> currentProperties);

  Vertex addCollection(String collectionName);
}
