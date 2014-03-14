package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class ValidatorManager {

  public Validator getValidatorFor(Class<? extends DomainEntity> type) {
    if (Relation.class.equals(type)) {
      return new RelationValidator();
    }
    return new NoOpValidator();

  }

}
