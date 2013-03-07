package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.util.Datable;
import nl.knaw.huygens.repository.model.util.IDPrefix;
import nl.knaw.huygens.repository.storage.Storage;

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
  
  @IndexAnnotation(fieldName = "birthDate")
  public Datable getBirthDate(){
    return this.birthDate;
  }
  
  @IndexAnnotation(fieldName = "deathDate")
  public Datable getDeathDate(){
    return this.deathDate;
  }
  
}
