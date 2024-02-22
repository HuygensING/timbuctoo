package nl.knaw.huygens.timbuctoo.datastores.quadstore.dto;

import nl.knaw.huygens.timbuctoo.util.Streams;
import nl.knaw.huygens.timbuctoo.util.Graph;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value.Immutable
public interface QuadGraphs {
  String getSubject();

  String getPredicate();

  String getObject();

  Optional<String> getValuetype();

  Optional<String> getLanguage();

  Direction getDirection();

  Set<Graph> getGraphs();

  @Value.Auxiliary
  ChangeType getChangeType();

  default boolean equalsIgnoreGraphs(QuadGraphs other) {
    return getSubject().equals(other.getSubject()) &&
        getPredicate().equals(other.getPredicate()) &&
        getObject().equals(other.getObject()) &&
        Objects.equals(getValuetype(), other.getValuetype()) &&
        Objects.equals(getLanguage(), other.getLanguage()) &&
        getDirection().equals(other.getDirection());
  }

  static QuadGraphs create(String subject, String predicate, Direction direction, String object,
                           Optional<String> valueType, Optional<String> language,
                           Collection<Graph> graphs, ChangeType changeType) {
    return ImmutableQuadGraphs.builder()
                              .subject(subject)
                              .predicate(predicate)
                              .object(object)
                              .valuetype(valueType)
                              .language(language)
                              .direction(direction)
                              .graphs(graphs)
                              .changeType(changeType)
                              .build();
  }

  static Stream<QuadGraphs> mapToQuadGraphs(Stream<CursorQuad> quadStream) {
    return Streams.combine(quadStream, CursorQuad::equalsExcludeGraph, ArrayList::new).map(quads -> {
      CursorQuad quad = quads.getFirst();

      Set<ChangeType> changeTypes = quads.stream().map(CursorQuad::getChangeType).collect(Collectors.toSet());
      ChangeType changeType = changeTypes.contains(ChangeType.ASSERTED) ?
          ChangeType.ASSERTED :
          (changeTypes.contains(ChangeType.RETRACTED) ? ChangeType.RETRACTED : ChangeType.UNCHANGED);

      List<Graph> graphs = quads.stream()
                                .map(q -> new Graph(q.getGraph().orElse(null)))
                                .collect(Collectors.toList());

      return QuadGraphs.create(
          quad.getSubject(),
          quad.getPredicate(),
          quad.getDirection(),
          quad.getObject(),
          quad.getValuetype(),
          quad.getLanguage(),
          graphs,
          changeType
      );
    });
  }
}
