package nl.knaw.huygens.timbuctoo.validation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Validator<T extends DomainEntity> {

  /**
   * Validates {@code entityToValidate}.
   * @param entityToValidate
   * @throws IOException 
   */
  public void validate(T entityToValidate) throws ValidationException, IOException;
}
