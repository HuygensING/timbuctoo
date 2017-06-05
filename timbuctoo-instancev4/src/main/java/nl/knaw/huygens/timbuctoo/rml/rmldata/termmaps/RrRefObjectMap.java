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
  private final String outputField;

  //Every time the referenced triplesMap creates a subject, the RrRefObjectMap will tell it's own datasource to store it
  public RrRefObjectMap(RrTriplesMap otherMap, RrJoinCondition rrJoinCondition, DataSource ownSource) {
    this.parentTriplesMapUri = otherMap.getUri();
    this.rrJoinCondition = rrJoinCondition;
    this.dataSource = ownSource;
    this.outputField = UUID.randomUUID().toString();
    otherMap.subscribeToSubjectsWith(this, rrJoinCondition.getParentField());
  }

  public Stream<Node> generateValue(Row input) {
    final List<String> result = input.getJoinValue(outputField);

    if (result == null || result.isEmpty()) {
      input.handleLinkError(
        rrJoinCondition.getChildField(),
        parentTriplesMapUri,
        rrJoinCondition.getParentField()
      );
      return Stream.empty();
    }

    return result.stream().map(NodeFactory::createURI);
  }

  /**
   * Every time a referenced triplesMap creates a subject, the RrRefObjectMap will tell it's own datasource to store it
   *
   * @param value the row from the source record
   * @param subject the subject under which the source record is now known
   */
  public void onNewSubject(String value, Node subject) {
    dataSource.willBeJoinedOn(rrJoinCondition.getChildField(), value, subject.getURI(), outputField);
  }


  @Override
  public String toString() {
    return String.format("      References %s on %s using %s\n",
      this.rrJoinCondition.getParentField(),
      this.parentTriplesMapUri,
      this.rrJoinCondition.getChildField()
    );
  }
}
