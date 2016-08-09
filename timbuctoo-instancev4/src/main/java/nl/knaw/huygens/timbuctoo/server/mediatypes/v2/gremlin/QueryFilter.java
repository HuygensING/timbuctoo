package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(name = "relation", value = RelationFilter.class),
  @JsonSubTypes.Type(name = "property", value = PropertyFilter.class),
  @JsonSubTypes.Type(name = "entity", value = CollectionQuery.class),
  @JsonSubTypes.Type(name = "value", value = PropertyEqualsFilter.class),
  @JsonSubTypes.Type(name = "between", value = PropertyBetweenFilter.class)})
public interface QueryFilter extends QueryStep {

  void setVres(Vres vres);
}
