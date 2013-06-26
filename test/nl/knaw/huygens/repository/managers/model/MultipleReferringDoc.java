package nl.knaw.huygens.repository.managers.model;

import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.annotations.RelatedDocument;
import nl.knaw.huygens.repository.annotations.RelatedDocuments;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@RelatedDocuments({ @RelatedDocument(type = ReferredDoc.class, accessors = { "referredDoc" }), @RelatedDocument(type = ReferringDoc.class, accessors = { "referringDoc" }) })
public class MultipleReferringDoc extends Document {

  private ReferredDoc referredDoc;
  private ReferringDoc referringDoc;

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
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  public ReferredDoc getReferredDoc() {
    return referredDoc;
  }

  public void setReferredDoc(ReferredDoc referredDoc) {
    this.referredDoc = referredDoc;
  }

  public ReferringDoc getReferringDoc() {
    return referringDoc;
  }

  public void setReferringDoc(ReferringDoc referringDoc) {
    this.referringDoc = referringDoc;
  }

}
