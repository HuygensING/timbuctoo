package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.model.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("USR")
@DocumentTypeName("user")
public class User extends SystemDocument {
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
