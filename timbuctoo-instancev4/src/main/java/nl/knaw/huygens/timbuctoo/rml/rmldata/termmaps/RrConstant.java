package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class RrConstant implements RrTermMap {
  private final Node value;

  public RrConstant(String value) {
    this.value = NodeFactory.createURI(value);
  }

  @Override
  public Node generateValue(Row input) {
    return value;
  }

}
