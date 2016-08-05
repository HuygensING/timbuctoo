package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

public interface Saver {
  Vertex addEntity(Vertex collection, Map<String, Object> currentProperties);

  Vertex addCollection(String collectionName);

  void addPropertyDescriptions(Vertex collection, ImportPropertyDescriptions importPropertyDescriptions);
}
