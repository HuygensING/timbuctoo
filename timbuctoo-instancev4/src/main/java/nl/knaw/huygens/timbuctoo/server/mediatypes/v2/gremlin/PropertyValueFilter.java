package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(name = "value", value = PropertyEqualsFilter.class),
        @JsonSubTypes.Type(name = "between", value = PropertyBetweenFilter.class)})
public interface PropertyValueFilter extends QueryStep {

  PropertyValueFilter setName(String name);

  PropertyValueFilter setDomain(String name);
}
