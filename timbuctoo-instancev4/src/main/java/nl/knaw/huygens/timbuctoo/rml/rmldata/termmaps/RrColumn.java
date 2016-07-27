package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.Map;

public class RrColumn implements RrTermMap {
  private final String referenceString;
  private TermType termType;
  private boolean isUsedInObjectMap;

  public RrColumn(String referenceString) {
    this.referenceString = referenceString;
  }

  public RrColumn(String referenceString, TermType termType) {
    this.referenceString = referenceString;
    this.termType = termType;
  }

  @Override
  public Node generateValue(Map<String, Object> input) {
    if (termType == null) {
      if (isUsedInObjectMap) {
        termType = TermType.Literal;
      } else {
        termType = TermType.IRI;
      }
    }
    switch (termType) {
      case IRI:
        return ModelFactory.createDefaultModel().createResource("" + input.get(referenceString)).asNode();//FIXME use NodeFactory
      case BlankNode:
        throw new UnsupportedOperationException("");//FIXME: implement
      case Literal:
        return ModelFactory.createDefaultModel().createLiteral("" + input.get(referenceString)).asNode();
      default:
        throw new UnsupportedOperationException("Not all items in the Enumerable where handled");
    }
  }

  @Override
  public void isUsedInObjectMap() {
    this.isUsedInObjectMap = true;
  }
}
