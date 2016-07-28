package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import org.apache.jena.graph.Node;

import java.util.Map;

public interface RrTermMap {
  Node generateValue(Map<String, Object> input);

}
