package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class RrRefObjectMap {
  private final String parentTriplesMapUri;
  private final RrJoinCondition rrJoinCondition;
  private final DataSource dataSource;
  private final String uniqueId;

  //Every time the referenced triplesMap creates a subject, the RrRefObjectMap will tell it's own datasource to store it
  public RrRefObjectMap(RrTriplesMap otherMap, RrJoinCondition rrJoinCondition, DataSource ownSource) {
    this.parentTriplesMapUri = otherMap.getUri();
    this.rrJoinCondition = rrJoinCondition;
    this.dataSource = ownSource;
    this.uniqueId = UUID.randomUUID().toString();
    otherMap.subscribeToSubjectsWith(this, rrJoinCondition.getParent());
  }

  public Stream<Node> generateValue(Row input) {
    final Object result = input.get(uniqueId);

    if (result == null) {
      input.handleLinkError(
        rrJoinCondition.getChild(),
        parentTriplesMapUri,
        rrJoinCondition.getParent()
      );
      return Stream.empty();
    }

    if (result instanceof List) {
      return ((List<?>) result).stream().map(v -> NodeFactory.createURI("" + v));
    } else {
      return Stream.of(NodeFactory.createURI("" + result));
    }
  }

  public void onNewSubject(Object value, Node subject) {
    dataSource.willBeJoinedOn(rrJoinCondition.getChild(), value, subject.getURI(), uniqueId);
  }

}
