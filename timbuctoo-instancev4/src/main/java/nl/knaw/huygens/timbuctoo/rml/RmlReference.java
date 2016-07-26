package nl.knaw.huygens.timbuctoo.rml;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.Map;

public class RmlReference implements TermMapContent {
  private final String referenceString;

  public RmlReference(String referenceString) {
    this.referenceString = referenceString;
  }

  @Override
  public Node generateValue(Map<String, Object> input) {
    return ModelFactory.createDefaultModel().createLiteral("" + input.get(referenceString)).asNode();
  }
}
