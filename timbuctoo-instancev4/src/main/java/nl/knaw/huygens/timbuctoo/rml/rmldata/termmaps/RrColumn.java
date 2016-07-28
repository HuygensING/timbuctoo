package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Map;
import java.util.stream.Stream;

public class RrColumn implements RrTermMap {
  private final String referenceString;
  private TermType termType;
  private boolean isUsedInObjectMap;

  public RrColumn(boolean isUsedInObjectMap, String referenceString) {
    this.isUsedInObjectMap = isUsedInObjectMap;
    this.referenceString = referenceString;
  }

  public RrColumn(boolean isUsedInObjectMap, String referenceString, TermType termType) {
    this.isUsedInObjectMap = isUsedInObjectMap;
    this.referenceString = referenceString;
    this.termType = termType;
  }

  @Override
  public Stream<Node> generateValue(Map<String, Object> input) {
    if (termType == null) {
      if (isUsedInObjectMap) {
        termType = TermType.Literal;
      } else {
        termType = TermType.IRI;
      }
    }
    switch (termType) {
      case IRI:
        return Stream.of(NodeFactory.createURI("" + input.get(referenceString)));
      case BlankNode:
        throw new UnsupportedOperationException("");//FIXME: implement
      case Literal:
        return Stream.of(NodeFactory.createLiteral("" + input.get(referenceString)));
      default:
        throw new UnsupportedOperationException("Not all items in the Enumerable where handled");
    }
  }
}
