package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources.UriSource;
import org.apache.jena.graph.Node_Literal;

import java.util.Optional;

public class RmlLogicalSource {
  private RmlSource source;
  private Node_Literal iterator;
  private Optional<Node_Literal> referenceFormulation = Optional.empty();

  public RmlSource getSource() {
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
    private final RmlLogicalSource instance;

    public Builder() {
      this.instance = new RmlLogicalSource();
    }

    RmlLogicalSource build() {
      return instance;
    }

    public Builder withSource(String source) {
      instance.source = new UriSource(source);
      return this;
    }
  }
}
