package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Validator {

  /**
   * Validates {@code entityToValidate}.
   * @param entityToValidate
   */
  public <T extends DomainEntity> void validate(T entityToValidate) throws ValidationException;
}
