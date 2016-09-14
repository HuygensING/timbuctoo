package nl.knaw.huygens.timbuctoo.database.dto;

import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.Change;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A custom dto for retrieving Women Writers entities.
 */
public class WwReadEntity extends ReadEntity {

  private final ReadEntity entity;
  private final Set<String> languages;

  public WwReadEntity(ReadEntity entity, Set<String> languages) {
    this.entity = entity;
    this.languages = languages;
  }

  public void setProperties(List<TimProperty<?>> properties) {
    entity.setProperties(properties);
  }

  public Iterable<TimProperty<?>> getProperties() {
    return entity.getProperties();
  }

  public void setRev(int rev) {
    entity.setRev(rev);
  }

  public int getRev() {
    return entity.getRev();
  }

  public void setDeleted(Boolean deleted) {
    entity.setDeleted(deleted);
  }

  public boolean getDeleted() {
    return entity.getDeleted();
  }

  public void setPid(String pid) {
    entity.setPid(pid);
  }

  public String getPid() {
    return entity.getPid();
  }

  public void setTypes(List<String> types) {
    entity.setTypes(types);
  }

  public List<String> getTypes() {
    return entity.getTypes();
  }

  public void setModified(Change modified) {
    entity.setModified(modified);
  }

  public Change getModified() {
    return entity.getModified();
  }

  public void setCreated(Change created) {
    entity.setCreated(created);
  }

  public Change getCreated() {
    return entity.getCreated();
  }

  public void setRelations(List<RelationRef> relations) {
    entity.setRelations(relations);
  }

  public List<RelationRef> getRelations() {
    return entity.getRelations();
  }

  public void setDisplayName(String displayName) {
    entity.setDisplayName(displayName);
  }

  public String getDisplayName() {
    return entity.getDisplayName();
  }

  public void setId(UUID id) {
    entity.setId(id);
  }

  public UUID getId() {
    return entity.getId();
  }

  public Set<String> getLanguages() {
    return languages;
  }
}
