package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfCreator;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;

@Value.Immutable
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
