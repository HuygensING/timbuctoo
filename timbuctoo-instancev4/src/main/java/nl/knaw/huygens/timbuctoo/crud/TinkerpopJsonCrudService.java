package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.io.IOException;
import java.util.UUID;

public class TinkerpopJsonCrudService {

  private final GraphWrapper graphwrapper;

  public TinkerpopJsonCrudService(GraphWrapper graphwrapper) {
    this.graphwrapper = graphwrapper;
  }

  public UUID create(ObjectNode input) throws InvalidCollectionException, IOException { //FIXME: actually throw these exceptions when appropriate
    return null;
  }
}
