package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class RrRefObjectMap implements RrTermMap {
  private RrTriplesMap parentTriplesMap;
  private RrJoinCondition rrJoinCondition;
  private String uniqueId;
  private DataSource dataSource;
  private String rrTriplesMapUri;

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
    parentTriplesMap.subscribeToSubjectsWith(this, this.rrJoinCondition.getParent());
  }

  public void newSubject(Object value, Node subject) {
    dataSource.willBeJoinedOn(rrJoinCondition.getChild(), value, subject.getURI(), uniqueId);
  }

  public void moveOver(String otherTriplesMap, DataSource otherDataSource) {
    this.dataSource = otherDataSource;
    this.rrTriplesMapUri = otherTriplesMap;
  }

  public void fixupTripleMaps(Function<String, RrTriplesMap> getter) {
    parentTriplesMap = getter.apply(rrTriplesMapUri);
    subscribeToParent();
  }

  public String getReferingTripleMap() {
    return rrTriplesMapUri;
  }

  public static class Builder {
    private RrRefObjectMap instance;

    public Builder() {
      this.instance = new RrRefObjectMap();
    }

    public Builder withParentTriplesMap(String rrTriplesMapUri) {
      instance.rrTriplesMapUri = rrTriplesMapUri;
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
  }

}
