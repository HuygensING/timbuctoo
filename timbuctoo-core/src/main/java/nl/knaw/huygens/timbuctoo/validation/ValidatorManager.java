package nl.knaw.huygens.timbuctoo.validation;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;

import com.google.inject.Inject;

public class ValidatorManager {

  private final Storage storage;

  @Inject
  public ValidatorManager(Storage storage) {
    this.storage = storage;
  }

  @SuppressWarnings("unchecked")
  public <T extends DomainEntity> Validator<T> getValidatorFor(Class<T> type) {
    if (Relation.class.isAssignableFrom(type)) {
      return (Validator<T>) new RelationValidatorFactory(storage).createRelationValidator();
    }
    return new NoOpValidator<T>();

  }

}
