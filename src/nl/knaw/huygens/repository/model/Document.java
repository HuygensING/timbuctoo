package nl.knaw.huygens.repository.model;


import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.storage.Storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
  public abstract String getDescription();

  public abstract void fetchAll(Storage storage);

  public static Class<? extends Document> getSubclassByString(String type) {
    return getSubclassByString(type, Document.class.getPackage().getName());
  }

  @SuppressWarnings("unchecked")
  public static Class<? extends Document> getSubclassByString(String type, String containerPackage) {
    if (type == null || type.length() < 2) {
      return null;
    }
    String className = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
    className = containerPackage + "." + className;
    try {
      Class<?> cls = Class.forName(className);
      if (Document.class.isAssignableFrom(cls)) {
        return (Class<? extends Document>) cls;
      }
      return null;
    } catch (ClassNotFoundException e) {
      return null;
    } catch (ClassCastException e) {
      return null;
    }
  }
}
