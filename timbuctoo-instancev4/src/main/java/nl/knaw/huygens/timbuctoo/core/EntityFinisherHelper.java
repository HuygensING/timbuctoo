package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.Logger;

import java.time.Clock;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.rdf.Database.RDF_URI_PROP;
import static org.slf4j.LoggerFactory.getLogger;

public class EntityFinisherHelper {
  private static final Logger LOG = getLogger(EntityFinisherHelper.class);
  private final Change change;

  // TODO let TimbuctooActions use this class
  public EntityFinisherHelper() {
    change = new Change();
    change.setTimeStamp(Clock.systemDefaultZone().instant().toEpochMilli()); // TODO make configurable
    change.setUserId("rdfImporter"); // TODO make configurable
  }

  public UUID newId(Vertex vertex, String vreName) {
    VertexProperty<Object> property = vertex.property(RDF_URI_PROP);
    UUID id = UUID.randomUUID();
    if (property.isPresent()) {
      try {
        String rdfUri = (String) property.value();
        if (rdfUri.startsWith("http://timbuctoo.huygens.knaw.nl/mapping/" + vreName + "/")) {
          String potentialTimId = rdfUri.substring(rdfUri.lastIndexOf('/') + 1);
          if (potentialTimId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            id = UUID.fromString(potentialTimId);
          }
        }
      } catch (Exception e) {
        LOG.error("Can't get timid from rdf uri. Either my guards are to lax or the parser not smart enough " +
          vertex.id(), e);
      }
    }

    return id;
  }

  public int getRev() {
    return 1;
  }

  public Change getChangeTime() {
    return change;
  }
}
