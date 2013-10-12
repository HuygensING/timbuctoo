package nl.knaw.huygens.timbuctoo.variation.model;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

/**
 * Basic POJO object used for testing
 */
public abstract class TestBaseDoc extends SystemEntity {

  /** Simple string prop for testing */
  public String name;

  @Override
  public String getDisplayName() {
    return name;
  }

}
