package nl.knaw.huygens.timbuctoo.rml;

import org.apache.jena.graph.Node;

import java.util.Map;

public interface TermMapContent {
  Node generateValue(Map<String, Object> input);
}
