package nl.knaw.huygens.timbuctoo.storage.graph;

public class CorruptVertexException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CorruptVertexException(Object id) {
    super(String.format("Node with id \"%s\" has no label of primitive type.", id));
  }

}
