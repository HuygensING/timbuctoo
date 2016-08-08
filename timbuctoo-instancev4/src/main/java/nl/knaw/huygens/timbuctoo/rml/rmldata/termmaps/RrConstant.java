package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;

import java.util.stream.Stream;

public class RrConstant implements RrTermMap {
  private final Node value;

  public RrConstant(Node value) {
    this.value = value;
  }

  @Override
  public Stream<Node> generateValue(Row input) {
    return Stream.of(value);
  }

}
