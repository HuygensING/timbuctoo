package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import org.apache.jena.graph.Node;

import java.util.Map;
import java.util.stream.Stream;

public class RrConstant implements RrTermMap {
  private final Node value;

  public RrConstant(Node value) {
    this.value = value;
  }

  @Override
  public Stream<Node> generateValue(Map<String, Object> input) {
    return Stream.of(value);
  }

}
