package nl.knaw.huygens.repository.model;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.Storage;

public abstract class Document {

  @NotNull
  @Pattern(regexp = "[A-Z]{3}\\d{10}")
  private String id;

  private int rev;

  private Change lastChange;

  private Change creation;

  private boolean _deleted;

  private String pid; // the persistent identifier.

  private List<String> variations = Lists.newArrayList();

  @JsonProperty("_id")
  @IndexAnnotation(fieldName = "id")
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

  @JsonProperty("^pid")
  public String getPid() {
    return pid;
  }

  @JsonProperty("^pid")
  public void setPid(String pid) {
    this.pid = pid;
  }

  @JsonProperty("@variations")
  public List<String> getVariations() {
    return variations;
  }

  @JsonProperty("@variations")
  public void setVariations(List<String> variations) {
    this.variations = variations;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public abstract String getDescription();

  public abstract void fetchAll(Storage storage);

}
