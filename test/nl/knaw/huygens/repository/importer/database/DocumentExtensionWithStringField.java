package nl.knaw.huygens.repository.importer.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;

public class DocumentExtensionWithStringField extends Document {

  public DocumentExtensionWithStringField() {
  }

  private String test;

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

  public void setTest(String test) {
    this.test = test;
  }

  public String getTest() {
    return this.test;
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