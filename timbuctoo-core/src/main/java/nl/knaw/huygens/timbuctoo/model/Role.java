package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

  @JsonProperty("@roleName")
  public String getRoleName() {
    return TypeNames.getInternalName(getClass());
  }

  @Override
  @JsonProperty("@variationRefs")
  //Should ignore the variationRefs during deserialization.
  @JsonIgnoreProperties(ignoreUnknown = true)
  public List<Reference> getVariationRefs() {
    return variationRefs;
  }

}
