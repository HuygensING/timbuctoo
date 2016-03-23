package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(name = "relation", value = RelationFilter.class),
  @JsonSubTypes.Type(name = "property", value = PropertyFilter.class),
  @JsonSubTypes.Type(name = "entity", value = CollectionQuery.class)})
public interface QueryFilter extends QueryStep {

}
