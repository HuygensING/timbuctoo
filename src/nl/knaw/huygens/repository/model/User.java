package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.model.util.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("USR")
public class User extends Document {
  public String pwHash;
  public String email;
  public String firstName;
  public String lastName;
  public List<String> groups;
  private String currentVariation;

  @Override
  @JsonIgnore
  public String getDescription() {
    return firstName + " " + lastName;
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
