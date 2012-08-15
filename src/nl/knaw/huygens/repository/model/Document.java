package nl.knaw.huygens.repository.model;


import org.codehaus.jackson.annotate.JsonProperty;


public abstract class Document {
  protected String type;

  private String id;
  private String revision;

  private Change lastChange;

  private boolean _deleted;

  @JsonProperty("_id")
  public String getId() {
    return id;
  }

  @JsonProperty("_id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("_rev")
  public String getRevision() {
    return revision;
  }

  @JsonProperty("_rev")
  public void setRevision(String rev) {
    this.revision = rev;
  }

  public String getType() {
    return type;
  }
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


  @JsonProperty("^deleted")
  public boolean isDeleted() {
    return _deleted;
  }

  @JsonProperty("^deleted")
  public void setDeleted(boolean deleted) {
    this._deleted = deleted;
  }

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
