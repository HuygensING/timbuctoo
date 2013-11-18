package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An abstract super class to define "functions" of {@code Entities}. For example a {@code Person}
 * could have have the {@code Role} {@code Scientist}
 * @author martijnm
 *
 */
public abstract class Role implements Variable {
  private List<Reference> varations;
  private String currentVariation;

  private final String roleName = this.getClass().getSimpleName();

  @JsonProperty("@roleName")
  public String getRoleName() {
    return roleName;
  }

  @Override
  @JsonProperty("@variations")
  public List<Reference> getVariations() {
    return varations;
  }

  @Override
  @JsonProperty("@variations")
  public void setVariations(List<Reference> variations) {
    this.varations = variations;

  }

  @Override
  public void addVariation(Class<? extends Entity> refType, String refId) {
    this.varations.add(new Reference(refType, refId));

  }

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    return this.currentVariation;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String currentVariation) {
    this.currentVariation = currentVariation;
  }

}
