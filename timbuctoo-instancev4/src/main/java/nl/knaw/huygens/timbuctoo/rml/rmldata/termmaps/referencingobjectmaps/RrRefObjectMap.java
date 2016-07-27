package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Map;
import java.util.UUID;

public class RrRefObjectMap implements RrTermMap {
  public final RrTriplesMap parentTriplesMap;
  public final RrJoinCondition rrJoinCondition;
  public final String uniqueId;

  public RrRefObjectMap(RrTriplesMap parentTriplesMap, RrJoinCondition joinCondition) {
    this.parentTriplesMap = parentTriplesMap;
    this.rrJoinCondition = joinCondition;
    this.uniqueId = UUID.randomUUID().toString();
    parentTriplesMap.willBeUsedInJoinLaterOn(joinCondition.parent, joinCondition.child, uniqueId);
  }

  @Override
  public Node generateValue(Map<String, Object> input) {
    final Object gotten = input.get(uniqueId);
    final Node uri = NodeFactory.createURI("" + gotten);
    return uri;
  }

  @Override
  public void isUsedInObjectMap() {
  }
}
