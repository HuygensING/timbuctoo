package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.Optional;
import java.util.stream.Stream;


public class RrPredicateObjectMapOfTermMap implements RrPredicateObjectMap {

  private final RrTermMap objectMap;
  private final RrTermMap predicateMap;

  public RrPredicateObjectMapOfTermMap(RrTermMap predicateMap, RrTermMap objectMap) {
    this.predicateMap = predicateMap;
    this.objectMap = objectMap;
  }

  @Override
  public Stream<Triple> generateValue(Node subject, Row row) {
    Optional<Node> predicate = predicateMap.generateValue(row);
    Optional<Node> object = objectMap.generateValue(row);
    if (predicate.isPresent() && object.isPresent()) {
      return Stream.of(new Triple(subject, predicate.get(), object.get()));
    } else {
      return Stream.empty();
    }
  }

  @Override
  public String toString() {
    return String.format("    predicateMap:\n%s    objectMap:\n%s    ================================\n",
      this.predicateMap,
      this.objectMap
    );
  }

}
