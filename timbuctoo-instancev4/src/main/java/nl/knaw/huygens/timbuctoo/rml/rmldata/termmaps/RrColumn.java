package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.stream.Stream;

public class RrColumn implements RrTermMap {
  private final String referenceString;
  private TermType termType;

  public RrColumn(String referenceString, TermType termType) {
    this.referenceString = referenceString;
    this.termType = termType;
  }

  @Override
  public Stream<Node> generateValue(Row input) {
    switch (termType) {
      case IRI:
        return Stream.of(NodeFactory.createURI("" + input.get(referenceString)));
      case BlankNode:
        return Stream.of(NodeFactory.createBlankNode("" + input.get(referenceString)));
      case Literal:
        return Stream.of(NodeFactory.createLiteral("" + input.get(referenceString)));
      default:
        throw new UnsupportedOperationException("Not all items in the Enumerable where handled");
    }
  }
}
