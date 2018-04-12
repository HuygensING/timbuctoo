package nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize(as = ImmutableSimplePath.class)
@JsonSerialize(as = ImmutableSimplePath.class)
public interface SimplePath extends SummaryProp {

  @JsonCreator
  static SummaryProp create(@JsonProperty("path") List<String> path) {
    return ImmutableSimplePath.builder().path(path).build();
  }

  List<String> getPath();
}
