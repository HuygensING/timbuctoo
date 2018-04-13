package nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.immutables.value.Value;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value.Immutable
@JsonDeserialize(as = ImmutableSimplePath.class)
@JsonSerialize(as = ImmutableSimplePath.class)
public interface SimplePath extends SummaryProp {

  @JsonCreator
  static SummaryProp create(@JsonProperty("path") List<String> simplePath) {
    return ImmutableSimplePath.builder().simplePath(simplePath).build();
  }

  List<String> getSimplePath();

  @JsonIgnore
  default List<DirectionalStep> getPath() {
    return getSimplePath().stream()
              .map(step -> DirectionalStep.create(step, Direction.OUT))
              .collect(toList());
  }
}
