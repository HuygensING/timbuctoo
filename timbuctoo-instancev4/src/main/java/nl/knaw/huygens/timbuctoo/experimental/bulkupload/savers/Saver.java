package nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;

public interface Saver {
  Vertex addEntity(Vertex collection, HashMap<String, Object> currentProperties);

  Vertex addCollection(String collectionName);
}
