package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Map;
import java.util.UUID;

public class RrRefObjectMap implements RrTermMap {
  private RrTriplesMap parentTriplesMap;
  private RrJoinCondition rrJoinCondition;
  private String uniqueId;

  public RrRefObjectMap() {
    this.uniqueId = UUID.randomUUID().toString();
  }

  @Override
  public Node generateValue(Map<String, Object> input) {
    return NodeFactory.createURI("" + input.get(uniqueId));
  }

  public RrTriplesMap getParentTriplesMap() {
    return parentTriplesMap;
  }

  public RrJoinCondition getRrJoinCondition() {
    return rrJoinCondition;
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public static Builder rrRefObjectMap() {
    return new Builder();
  }

  public static class Builder {
    private RrRefObjectMap instance;

    public Builder() {
      this.instance = new RrRefObjectMap();
    }

    public Builder withParentTriplesMap(String rrTriplesMapUri) {
      return this;
    }

    public Builder withJoinCondition(String child, String parent) {
      instance.rrJoinCondition = new RrJoinCondition(child, parent);
      return this;
    }

    public RrRefObjectMap build() {
      return this.instance;
    }
  }
}
