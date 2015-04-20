package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion;

public class CorruptNodeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CorruptNodeException(Object id) {
    super(String.format("Node with id \"%s\" has no label of primitive type.", id));
  }

}
