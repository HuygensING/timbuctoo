package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class NoOpValidator implements Validator {

  @Override
  public <T extends DomainEntity> void validate(T entityToValidate) throws ValidationException {
    // do nothing
  }

}
