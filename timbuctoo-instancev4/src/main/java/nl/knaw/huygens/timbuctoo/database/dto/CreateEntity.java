package nl.knaw.huygens.timbuctoo.database.dto;

import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.Change;

import java.util.UUID;


public class CreateEntity {
  private final Iterable<TimProperty<?>> properties;
  private UUID id;
  private Change created;

  public CreateEntity(Iterable<TimProperty<?>> properties) {
    this.properties = properties;
  }

  public Iterable<TimProperty<?>> getProperties() {
    return properties;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Change getCreated() {
    return created;
  }

  public void setCreated(Change created) {
    this.created = created;
  }
}
