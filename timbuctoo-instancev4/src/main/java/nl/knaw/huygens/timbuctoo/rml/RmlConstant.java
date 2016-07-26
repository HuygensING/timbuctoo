package nl.knaw.huygens.timbuctoo.rml;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.Map;

public class RmlConstant implements TermMapContent {
  private final String value;

  public RmlConstant(String value) {
    this.value = value;
  }

  @Override
  public Node generateValue(Map<String, Object> input) {
    return ModelFactory.createDefaultModel().createTypedLiteral(value).asNode();
  }
}
