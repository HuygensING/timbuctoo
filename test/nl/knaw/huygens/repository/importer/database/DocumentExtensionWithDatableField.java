package nl.knaw.huygens.repository.importer.database;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Datable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentExtensionWithDatableField extends Document {

  public DocumentExtensionWithDatableField() {

  }

  private Datable datable;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setDatable(Datable datable) {
    this.datable = datable;
  }

  public Datable getDatable() {
    return this.datable;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public String getCurrentVariation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public void setCurrentVariation(String defaultVRE) {
    // TODO Auto-generated method stub

  }

}