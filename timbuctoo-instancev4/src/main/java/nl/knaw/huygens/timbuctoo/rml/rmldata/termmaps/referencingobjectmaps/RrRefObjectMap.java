package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.TripleMapGetter;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Map;
import java.util.UUID;

public class RrRefObjectMap implements RrTermMap {
  private RrTriplesMap parentTriplesMap;
  private RrJoinCondition rrJoinCondition;
  private String uniqueId;
  private DataSource dataSource;

  public RrRefObjectMap() {
    this.uniqueId = UUID.randomUUID().toString();
  }

  @Override
  public Node generateValue(Map<String, Object> input) {
    return NodeFactory.createURI("" + input.get(uniqueId));
  }

  public static Builder rrRefObjectMap() {
    return new Builder();
  }

  private void subscribeToParent() {
    this.parentTriplesMap.subscribeToSubjectsWith(this, this.rrJoinCondition.getParent());
  }

  public void newSubject(Object value, Node subject) {
    dataSource.willBeJoinedOn(rrJoinCondition.getChild(), value, subject.getURI(), uniqueId);
  }

  public static class Builder {
    private RrRefObjectMap instance;
    private String rrTriplesMapUri;

    public Builder() {
      this.instance = new RrRefObjectMap();
    }

    public Builder withParentTriplesMap(String rrTriplesMapUri) {
      this.rrTriplesMapUri = rrTriplesMapUri;
      return this;
    }

    public Builder withJoinCondition(String child, String parent) {
      instance.rrJoinCondition = new RrJoinCondition(child, parent);
      return this;
    }

    public RrRefObjectMap build(DataSource dataSource) {
      this.instance.dataSource = dataSource;
      return this.instance;
    }

    public void fixupTripleMaps(TripleMapGetter getter) {
      final RrRefObjectMap instance = this.instance;
      instance.parentTriplesMap = getter.getTriplesMap(rrTriplesMapUri);
      instance.subscribeToParent();
    }
  }

}
