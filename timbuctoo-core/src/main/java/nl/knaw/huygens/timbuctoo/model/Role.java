package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An abstract super class to define "functions" of {@code Entities}. For example a {@code Person}
 * could have have the {@code Role} {@code Scientist}
 * @author martijnm
 */
//@see: http://wiki.fasterxml.com/JacksonPolymorphicDeserialization
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class Role implements Variable {

  private List<Reference> variationRefs;

  private final String roleName = this.getClass().getSimpleName();

  @JsonProperty("@roleName")
  public String getRoleName() {
    return roleName;
  }

  @Override
  @JsonProperty("@variationRefs")
  public List<Reference> getVariationRefs() {
    return variationRefs;
  }

  @Override
  @JsonProperty("@variationRefs")
  public void setVariationRefs(List<Reference> variationRefs) {
    this.variationRefs = variationRefs;
  }

}
