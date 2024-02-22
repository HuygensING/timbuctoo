package nl.knaw.huygens.timbuctoo.graphql.mutations;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.util.Graph;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.stream.Stream;

public class Change {
  private final Graph graph;
  private final String subject;
  private final String predicate;
  private final List<Value> values;
  private final Stream<Value> oldValues;

  public Change(Graph graph, String subject, String predicate, Value value) {
    this.graph = graph;
    this.subject = subject;
    this.predicate = predicate;

    this.values = Lists.newArrayList(value);
    this.oldValues = Stream.empty();
  }

  public Change(Graph graph, String subject, String predicate, List<Value> values, Stream<Value> oldValues) {
    this.graph = graph;
    this.subject = subject;
    this.predicate = predicate;

    this.values = values;
    this.oldValues = oldValues;
  }

  public Graph getGraph() {
    return graph;
  }

  public String getSubject() {
    return subject;
  }

  public String getPredicate() {
    return predicate;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    Change change = (Change) other;

    return new EqualsBuilder()
      .append(graph, change.graph)
      .append(subject, change.subject)
      .append(predicate, change.predicate)
      .append(values, change.values)
      .append(oldValues, change.oldValues)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(graph)
      .append(subject)
      .append(predicate)
      .append(values)
      .append(oldValues)
      .toHashCode();
  }

  @Override
  public String toString() {
    return "Change{" +
      "graph='" + graph + '\'' +
      ", subject='" + subject + '\'' +
      ", predicate='" + predicate + '\'' +
      ", values=" + values +
      ", oldValues=" + oldValues +
      '}';
  }

  public List<Value> getValues() {
    return values;
  }

  public Stream<Value> getOldValues() {
    return oldValues;
  }

  public record Value(String rawValue, String type) {
  }
}
