package nl.knaw.huygens.timbuctoo.graphql.mutations;

import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.List;
import java.util.stream.Stream;

public record Change(Graph graph, String subject, String predicate, List<Value> values, Stream<Value> oldValues) {
  public Change(Graph graph, String subject, String predicate, Value value) {
    this(graph, subject, predicate, List.of(value), Stream.empty());
  }

  public record Value(String rawValue, String type) {
  }
}
