package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IDPrefix("LEG")
@DocumentTypeName("legislation")
public class Legislation extends DomainDocument {

  private String title;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDescription() {
    return title;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

}
