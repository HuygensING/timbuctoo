package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfCreator;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableLogEntry.class)
@JsonDeserialize(as = ImmutableLogEntry.class)
public interface LogEntry {
  URI getName();

  Optional<RdfCreator> getRdfCreator();

  Optional<String> getLogToken();

  static LogEntry create(URI name, String token) {
    return ImmutableLogEntry.builder()
      .name(name)
      .logToken(token)
      .rdfCreator(Optional.empty())
      .build();
  }

  static LogEntry create(URI name, RdfCreator creator) {
    return ImmutableLogEntry.builder()
      .name(name)
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
