package nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface SummaryProp {
  @JsonCreator
  static SummaryProp create(@JsonProperty("path") List<String> path, @JsonProperty("type") String type) {
    return ImmutableSummaryProp.builder().path(path).type(type).build();
  }

  List<String> getPath();

  String getType();
}
