package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.storage.Storage;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IDPrefix("USR")
public class User extends Document {
  // FIXME separate IDs and email addresses.
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
