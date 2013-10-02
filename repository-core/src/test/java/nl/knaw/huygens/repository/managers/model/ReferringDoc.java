package nl.knaw.huygens.repository.managers.model;

import nl.knaw.huygens.repository.annotations.RelatedDocument;
import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@RelatedDocument(type = ReferredDoc.class, accessors = { "referedDoc" })
public class ReferringDoc extends Entity {

  private ReferredDoc referedDoc;

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String currentVariation) {
    // TODO Auto-generated method stub

  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  public ReferredDoc getReferedDoc() {
    return referedDoc;
  }

  public void setReferedDoc(ReferredDoc referedDoc) {
    this.referedDoc = referedDoc;
  }

}
