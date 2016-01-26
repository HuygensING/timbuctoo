package nl.knaw.huygens.timbuctoo.server;

import org.apache.tinkerpop.gremlin.structure.Graph;

public interface GraphWrapper {
  Graph getGraph();
}
