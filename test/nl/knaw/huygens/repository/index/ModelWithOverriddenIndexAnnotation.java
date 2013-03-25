package nl.knaw.huygens.repository.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;

public class ModelWithOverriddenIndexAnnotation extends Document {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "description")
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void fetchAll(Storage storage) {
    // TODO Auto-generated method stub

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
