package nl.knaw.huygens.repository.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.repository.model.util.IDPrefix;
import nl.knaw.huygens.repository.storage.Storage;

@IDPrefix("USR")
public class User extends Document {
  public String pwHash;
  public String email;
  public String firstName;
  public String lastName;
  public List<String> groups;
  private String defaultVRE;

  @Override
  @JsonIgnore
  public String getDescription() {
    return firstName + " " + lastName;
  }

  @Override
  public void fetchAll(Storage storage) {
    // No references, so this is empty.
  }
  
  @Override
  @JsonProperty("!defaultVRE")
  public String getDefaultVRE() {
    return defaultVRE;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public void setDefaultVRE(String defaultVRE) {
    this.defaultVRE = defaultVRE;
  }
}
