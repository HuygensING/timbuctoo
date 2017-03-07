package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;

import java.util.Optional;

public interface RrTermMap {
  Optional<Node> generateValue(Row input);

}
