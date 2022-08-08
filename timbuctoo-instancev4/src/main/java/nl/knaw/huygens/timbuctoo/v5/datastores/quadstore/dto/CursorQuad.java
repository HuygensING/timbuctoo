package nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto;

import nl.knaw.huygens.timbuctoo.v5.datastores.CursorValue;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;
import org.immutables.value.Value;

import java.util.Objects;
import java.util.Optional;

@Value.Immutable
public interface CursorQuad extends CursorValue {
  String getSubject();

  String getPredicate();

  String getObject();

  Optional<String> getValuetype();

  Optional<String> getLanguage();

  Optional<String> getGraph();

  Direction getDirection();

  @Value.Auxiliary
  ChangeType getChangeType();

  static CursorQuad create(String subject, String predicate, Direction direction, String object, String valueType,
                           String language, String graph, String cursor) {
    return create(subject, predicate, direction, ChangeType.UNCHANGED, object, valueType, language, graph, cursor);
  }

  static CursorQuad create(String subject, String predicate, Direction direction, ChangeType changeType, String object,
                           String valueType, String language, String graph, String cursor) {
    return ImmutableCursorQuad.builder()
                              .subject(subject)
                              .predicate(predicate)
                              .object(object)
                              .valuetype(Optional.ofNullable(valueType))
                              .language(Optional.ofNullable(language))
                              .graph(Optional.ofNullable(graph))
                              .cursor(cursor)
                              .direction(direction)
                              .changeType(changeType)
                              .build();
  }

  default boolean inGraph(Optional<Graph> graph) {
    if (graph.isEmpty()) {
      return true;
    }

    Graph filterGraph = graph.get();
    return (getGraph().isEmpty() && filterGraph.isDefaultGraph()) ||
        (getGraph().isPresent() && getGraph().get().equals(filterGraph.getUri()));
  }

  default boolean equalsExcludeGraph(CursorQuad other) {
    return other != null &&
        getSubject().equals(other.getSubject()) &&
        getPredicate().equals(other.getPredicate()) &&
        getObject().equals(other.getObject()) &&
        Objects.equals(getValuetype(), other.getValuetype()) &&
        Objects.equals(getLanguage(), other.getLanguage()) &&
        getDirection().equals(other.getDirection());
  }
}
