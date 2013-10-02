package nl.knaw.huygens.repository.tools.importer.database;

import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.model.util.Datable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentExtensionWithDatableField extends Entity {

  public DocumentExtensionWithDatableField() {

  }

  private Datable datable;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
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
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String defaultVRE) {
    // TODO Auto-generated method stub

  }

}