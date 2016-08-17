package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.stream.Stream;

public interface RrPredicateObjectMap {
  Stream<Triple> generateValue(Node subject, Row row);
}
