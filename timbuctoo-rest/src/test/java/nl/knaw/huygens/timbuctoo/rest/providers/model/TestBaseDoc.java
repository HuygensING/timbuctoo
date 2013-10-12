package nl.knaw.huygens.timbuctoo.rest.providers.model;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

/**
 * Basic POJO object used for testing
 */
public abstract class TestBaseDoc extends DomainEntity {

  /** Simple string prop for testing */
  public String name;

  @Override
  public String getDisplayName() {
    return name;
  }

}
