package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class RrColumn implements RrTermMap {
  private final String referenceString;
  private TermType termType;

  public RrColumn(String referenceString, TermType termType) {
    this.referenceString = referenceString;
    this.termType = termType;
  }

  @Override
  public Node generateValue(Row input) {
    switch (termType) {
      case IRI:
        return NodeFactory.createURI("" + input.get(referenceString));
      case BlankNode:
        return NodeFactory.createBlankNode("" + input.get(referenceString));
      case Literal:
        final Object value = input.get(this.referenceString);
        return NodeFactory.createLiteral(value == null ? "" : "" + value);
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
