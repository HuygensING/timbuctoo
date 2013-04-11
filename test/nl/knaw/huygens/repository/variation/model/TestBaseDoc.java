package nl.knaw.huygens.repository.variation.model;

import nl.knaw.huygens.repository.model.Document;

/**
 * Basic POJO object used for testing
 */
public abstract class TestBaseDoc extends Document {
  /**
   * Simple string prop for testing.
   */
  public String name;

  @Override
  public String getDescription() {
    return name;
  }

}
