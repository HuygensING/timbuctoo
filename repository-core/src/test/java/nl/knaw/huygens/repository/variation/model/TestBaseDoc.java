package nl.knaw.huygens.repository.variation.model;

import nl.knaw.huygens.repository.model.Entity;

/**
 * Basic POJO object used for testing
 */
public abstract class TestBaseDoc extends Entity {

  /** Simple string prop for testing */
  public String name;

  @Override
  public String getDisplayName() {
    return name;
  }

}
