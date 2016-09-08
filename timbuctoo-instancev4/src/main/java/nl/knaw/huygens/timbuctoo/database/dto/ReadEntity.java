package nl.knaw.huygens.timbuctoo.database.dto;

import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.Change;

import java.util.List;
import java.util.UUID;

public class ReadEntity {
  private List<TimProperty<?>> properties;
  private int rev;
  private boolean deleted;
  private String pid;
  private List<String> types;
  private Change modified;
  private Change created;
  private List<RelationRef> relations;
  private String displayName;
  private UUID id;


  public void setProperties(List<TimProperty<?>> properties) {
    this.properties = properties;
  }

  public Iterable<TimProperty<?>> getProperties() {
    return properties;
  }

  public void setRev(int rev) {
    this.rev = rev;
  }

  public int getRev() {
    return rev;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public boolean getDeleted() {
    return deleted;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }

  public String getPid() {
    return pid;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }

  public List<String> getTypes() {
    return types;
  }

  public void setModified(Change modified) {
    this.modified = modified;
  }

  public Change getModified() {
    return modified;
  }

  public void setCreated(Change created) {
    this.created = created;
  }

  public Change getCreated() {
    return created;
  }

  public void setRelations(List<RelationRef> relations) {
    this.relations = relations;
  }

  public List<RelationRef> getRelations() {
    return relations;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }
}
