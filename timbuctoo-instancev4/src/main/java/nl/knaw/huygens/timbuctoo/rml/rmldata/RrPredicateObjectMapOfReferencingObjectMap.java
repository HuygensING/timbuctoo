package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrRefObjectMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.stream.Stream;

public class RrPredicateObjectMapOfReferencingObjectMap implements RrPredicateObjectMap {

  private final RrTermMap predicateMap;
  private final boolean isInverted;
  private final RrRefObjectMap objectMap;

  public RrPredicateObjectMapOfReferencingObjectMap(RrTermMap predicateMap, boolean isInverted,
                                                    RrRefObjectMap objectMap) {
    this.predicateMap = predicateMap;
    this.isInverted = isInverted;
    this.objectMap = objectMap;
  }

  @Override
  public Stream<Triple> generateValue(Node subject, Row row) {
    Node predicate = predicateMap.generateValue(row);
    if (isInverted) {
      return objectMap
        .generateValue(row)
        .map(value -> new Triple(value, predicate, subject));
    } else {
      return objectMap
        .generateValue(row)
        .map(value -> new Triple(subject, predicate, value));
    }
  }

  @Override
  public String toString() {
    return String.format("    isInverted: %s\n    predicateMap:\n%s    objectMap:\n%s    " +
        "================================\n",
      this.isInverted,
      this.predicateMap,
      this.objectMap
    );
  }

}
