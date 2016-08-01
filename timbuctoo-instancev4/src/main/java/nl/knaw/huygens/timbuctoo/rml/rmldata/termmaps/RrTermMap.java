package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import org.apache.jena.graph.Node;

import java.util.Map;
import java.util.stream.Stream;

public interface RrTermMap {
  Stream<Node> generateValue(Map<String, Object> input);

}
