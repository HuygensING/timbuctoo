package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;

import java.util.stream.Stream;

public interface RrTermMap {
  Stream<Node> generateValue(Row input);

}
