package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.model.storage.Storage;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User extends Document {
  public String pwHash;
  public String firstName;
  public String lastName;
  public List<String> groups;

  @Override
  @JsonIgnore
  public String getDescription() {
    return firstName + " " + lastName;
  }

  @Override
  public void fetchAll(Storage storage) {
    // No references, so this is empty.
  }
}
