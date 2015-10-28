package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public final class ElementFields {
  private ElementFields() {
    throw new RuntimeException("Class is not meant to be instantiated.");
  }

  public static final String ELEMENT_TYPES = DomainEntity.DB_VARIATIONS_PROP_NAME;
  public static final String IS_LATEST = "isLatest";
}
