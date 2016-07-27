package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import org.apache.jena.graph.Node_URI;

public class RrPredicateObjectMap {
  public final Node_URI predicate;
  public final RrTermMap objectMap;

  public RrPredicateObjectMap(Node_URI predicate, RrTermMap objectMap) {
    this.predicate = predicate;
    this.objectMap = objectMap;
    this.objectMap.isUsedInObjectMap();
  }
}
