package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.model.storage.Storage;

@IDPrefix("PER")
public class Person extends Document {
  public String name;
  public Datable birthDate;
  public Datable deathDate;

  @Override
  public String getDescription() {
    return name;
  }

  @Override
  public void fetchAll(Storage storage) {
    // No references
  }
}
