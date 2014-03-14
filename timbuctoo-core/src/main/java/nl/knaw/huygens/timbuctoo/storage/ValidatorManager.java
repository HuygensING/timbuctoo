package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import com.google.inject.Inject;

public class ValidatorManager {

  private final Storage storage;

  @Inject
  public ValidatorManager(Storage storage) {
    this.storage = storage;
  }

  @SuppressWarnings("unchecked")
  public <T extends DomainEntity> Validator<T> getValidatorFor(Class<T> type) {
    if (Relation.class.equals(type)) {
      return (Validator<T>) new RelationValidator(storage);
    }
    return new NoOpValidator<T>();

  }

}
