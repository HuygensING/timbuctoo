package nl.knaw.huygens.repository.variation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;

public class TestExtraBaseDoc extends Document {
  private String defaultVRE;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
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
    return defaultVRE;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public void setDefaultVRE(String defaultVRE) {
    this.defaultVRE = defaultVRE;

  }

}
