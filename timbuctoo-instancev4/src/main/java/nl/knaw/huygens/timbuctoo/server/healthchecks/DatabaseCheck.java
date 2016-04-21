package nl.knaw.huygens.timbuctoo.server.healthchecks;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface DatabaseCheck {
  ValidationResult check(Vertex vertex);
}
