package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.model.util.Datable;
import nl.knaw.huygens.repository.model.util.IDPrefix;

import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("PER")
public class Person extends Document {
  public String name;
  public Datable birthDate;
  public Datable deathDate;
  private String currentVariation;

  @Override
  public String getDescription() {
    return name;
  }

  /*
   * IndexAnnotation commented out, because the setupscript does not create a good Solr-schema for Person.
   * The fields of birthDate and deathDate are missing. See http://suzanna.huygens.knaw.nl/issues/1397 for more information.
   *
   */
  //@IndexAnnotation(fieldName = "birthDate")
  public Datable getBirthDate() {
    return this.birthDate;
  }

  //@IndexAnnotation(fieldName = "deathDate")
  public Datable getDeathDate() {
    return this.deathDate;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public String getCurrentVariation() {
    return currentVariation;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public void setCurrentVariation(String currentVariation) {
    this.currentVariation = currentVariation;
  }

}
