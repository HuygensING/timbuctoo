package nl.knaw.huygens.timbuctoo.database.dto;

import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;


public class CreateEntity {
  private final Iterable<TimProperty<?>> properties;

  public CreateEntity(Iterable<TimProperty<?>> properties) {
    this.properties = properties;
  }

  public Iterable<TimProperty<?>> getProperties() {
    return properties;
  }
}
