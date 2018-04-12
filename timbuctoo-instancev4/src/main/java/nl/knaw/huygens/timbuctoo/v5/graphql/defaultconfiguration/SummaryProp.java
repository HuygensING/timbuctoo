package nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "SimplePath", value = ImmutableSimplePath.class),
    @JsonSubTypes.Type(name = "DirectionalPath", value = ImmutableDirectionalPath.class)
  })
public interface SummaryProp {

  static List<DirectionalStep> getDirectedPath(SummaryProp summaryProp)  {
    if (summaryProp instanceof SimplePath) {
      return ((SimplePath) summaryProp).getPath().stream()
                                       .map(step -> DirectionalStep.create(step, Direction.OUT))
                                       .collect(toList());
    } else if (summaryProp instanceof DirectionalPath) {
      return ((DirectionalPath) summaryProp).getPath();
    }
    return Lists.newArrayList();
  }

  static List<String> getUndirectedPath(SummaryProp summaryProp)  {
    if (summaryProp instanceof SimplePath) {
      return ((SimplePath) summaryProp).getPath();
    } else if (summaryProp instanceof DirectionalPath) {
      return ((DirectionalPath) summaryProp).getPath().stream()
                                            .map(DirectionalStep::getStep).collect(Collectors.toList());
    }
    return Lists.newArrayList();
  }
}
