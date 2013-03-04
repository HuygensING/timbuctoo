package nl.knaw.huygens.repository.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.Storage;

public abstract class Document {
  protected String type;

  private String id;

  private int rev;

  private Change lastChange;

  private Change creation;

  private boolean _deleted;

  @JsonProperty("_id")
  @IndexAnnotation(fieldName="id")
  public String getId() {
    return id;
  }

  @JsonProperty("_id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("^rev")
  public int getRev() {
    return rev;
  }

  @JsonProperty("^rev")
  public void setRev(int rev) {
    this.rev = rev;
  }
  
  @JsonProperty("^type")
  public String getType() {
    return type;
  }
  
  @JsonProperty("^type")
  public void setType(String type) {
    this.type = type;
  }


  @JsonProperty("^lastChange")
  public Change getLastChange() {
    return lastChange;
  }

  @JsonProperty("^lastChange")
  public void setLastChange(Change lastChange) {
    this.lastChange = lastChange;
  }

  @JsonProperty("^creation")
  public Change getCreation() {
    return creation;
  }

  @JsonProperty("^creation")
  public void setCreation(Change creation) {
    this.creation = creation;
  }

  @JsonProperty("^deleted")
  public boolean isDeleted() {
    return _deleted;
  }

  @JsonProperty("^deleted")
  public void setDeleted(boolean deleted) {
    this._deleted = deleted;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public abstract String getDescription();

  public abstract void fetchAll(Storage storage);
}
