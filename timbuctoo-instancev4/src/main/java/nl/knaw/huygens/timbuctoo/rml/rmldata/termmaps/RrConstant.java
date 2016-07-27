package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import org.apache.jena.graph.Node;

import java.util.Map;

public class RrConstant implements RrTermMap {
  private final Node value;

  public RrConstant(Node value) {
    this.value = value;
  }

  @Override
  public Node generateValue(Map<String, Object> input) {
    return value;
  }

  @Override
  public void isUsedInObjectMap() {
    //Doesn't matter
  }
}
