package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.util.Datable;
import nl.knaw.huygens.repository.model.util.IDPrefix;

import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("PER")
public class Person extends VariationDocument {

  public String name;
  public Datable birthDate;
  public Datable deathDate;
  private String currentVariation;

  @Override
  public String getDescription() {
    return name;
  }

  @IndexAnnotation(fieldName = "facet_t_name", isFaceted = true)
  public String getName() {
    return name;
  }

  @IndexAnnotation(fieldName = "facet_s_birthDate", isFaceted = true, canBeEmpty = true)
  public Datable getBirthDate() {
    return birthDate;
  }

  @IndexAnnotation(fieldName = "facet_s_deathDate", isFaceted = true, canBeEmpty = true)
  public Datable getDeathDate() {
    return deathDate;
  }

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    return currentVariation;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String currentVariation) {
    this.currentVariation = currentVariation;
  }

}
