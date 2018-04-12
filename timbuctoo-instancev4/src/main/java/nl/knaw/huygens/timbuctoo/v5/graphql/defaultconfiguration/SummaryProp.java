package nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "SimplePath", value = ImmutableSimplePath.class),
    @JsonSubTypes.Type(name = "DirectionalPath", value = ImmutableDirectionalPath.class)
  })
public interface SummaryProp {
}
