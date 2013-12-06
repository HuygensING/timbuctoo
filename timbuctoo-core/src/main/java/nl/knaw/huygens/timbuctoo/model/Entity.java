package nl.knaw.huygens.timbuctoo.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// Annotation determines to which subclass the entity has to be resolved.
// @see: http://wiki.fasterxml.com/JacksonPolymorphicDeserialization
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class Entity {

  @NotNull
  @Pattern(regexp = Paths.ID_REGEX)
  private String id;

  /** Revison number; also used for integrity of updates. */
  private int rev;
  /** Provides info about creation. */
  private Change creation;
  /** Provides info about last update. */
  private Change lastChange;

  /**
   * Returns the name to be displayed for identification of this entity.
   */
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public abstract String getDisplayName();

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

  @JsonProperty("^creation")
  public Change getCreation() {
    return creation;
  }

  @JsonProperty("^creation")
  public void setCreation(Change creation) {
    this.creation = creation;
  }

  @JsonProperty("^lastChange")
  public Change getLastChange() {
    return lastChange;
  }

  @JsonProperty("^lastChange")
  public void setLastChange(Change lastChange) {
    this.lastChange = lastChange;
  }

}
