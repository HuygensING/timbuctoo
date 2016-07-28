package nl.knaw.huygens.timbuctoo.rml.rmldata;

import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;

import java.util.Optional;

public class RrLogicalSource {
  private Node_URI source;
  private Node_Literal iterator;
  private Optional<Node_Literal> referenceFormulation = Optional.empty();

  public Node_URI getSource() {
    return source;
  }

  public Node_Literal getIterator() {
    return iterator;
  }

  public Optional<Node_Literal> getReferenceFormulation() {
    return referenceFormulation;
  }

  public static Builder rrLogicalSource() {
    return new Builder();
  }

  public static class Builder {
    private final RrLogicalSource instance;

    public Builder() {
      this.instance = new RrLogicalSource();
    }

    RrLogicalSource build() {
      return instance;
    }

    public Builder withSource(Node_URI source) {
      instance.source = source;
      return this;
    }
  }
}
