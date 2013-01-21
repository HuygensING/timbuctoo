package nl.knaw.huygens.repository.variation.base.model;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;

public abstract class TestBaseDoc extends Document {
  public String name;
  
  @Override
  public String getDescription() {
    return name;
  }

  @Override
  public void fetchAll(Storage storage) {
    // TODO Auto-generated method stub
    
  }
}
