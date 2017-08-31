package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfCreator;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableLogEntry.class)
@JsonDeserialize(as = ImmutableLogEntry.class)
public interface LogEntry {
  String getBaseUri();

  String getDefaultGraph();

  Optional<RdfCreator> getRdfCreator();

  Optional<String> getLogToken();

  static LogEntry create(String baseUri, String defaultGraph, String token) {
    return ImmutableLogEntry.builder()
      .baseUri(baseUri)
      .defaultGraph(defaultGraph)
      .logToken(token)
      .rdfCreator(Optional.empty())
      .build();
  }

  static LogEntry create(String baseUri, String defaultGraph, RdfCreator creator) {
    return ImmutableLogEntry.builder()
      .baseUri(baseUri)
      .defaultGraph(defaultGraph)
      .logToken(Optional.empty())
      .rdfCreator(creator)
      .build();
  }

  static LogEntry addLogToEntry(LogEntry entry, String token) {
    return ImmutableLogEntry
      .copyOf(entry)
      .withRdfCreator(Optional.empty())
      .withLogToken(token);
  }

}
