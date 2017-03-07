package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Optional;

public class RrColumn implements RrTermMap {
  private final String referenceString;
  private TermType termType;

  public RrColumn(String referenceString, TermType termType) {
    this.referenceString = referenceString;
    this.termType = termType;
  }

  @Override
  public Optional<Node> generateValue(Row input) {
    Object value = input.get(referenceString);
    if (value == null) {
      return Optional.empty();
    }

    switch (termType) {
      case IRI:
        return Optional.of(NodeFactory.createURI("" + value));
      case BlankNode:
        return Optional.of(NodeFactory.createBlankNode("" + value));
      case Literal:
        return Optional.of(NodeFactory.createLiteral("" + value));
      default:
        throw new UnsupportedOperationException("Not all items in the Enumerable where handled");
    }
  }

  @Override
  public String toString() {
    return String.format("      Column: %s (%s)\n",
      this.referenceString,
      this.termType
    );
  }

}
