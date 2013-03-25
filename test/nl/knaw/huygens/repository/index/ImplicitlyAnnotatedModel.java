package nl.knaw.huygens.repository.index;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;

public class ImplicitlyAnnotatedModel extends Document {

  @Override
  public String getId() {
    return "";
  }
  
  @Override
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void fetchAll(Storage storage) {
    // TODO Auto-generated method stub

  }

  @IndexAnnotation
  public String getString() {
    return "";
  }

  @Override
  @JsonProperty("!defaultVRE")
  public String getDefaultVRE() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public void setDefaultVRE(String defaultVRE) {
    // TODO Auto-generated method stub
    
  }

}
