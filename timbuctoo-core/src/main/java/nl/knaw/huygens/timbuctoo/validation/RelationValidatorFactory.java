package nl.knaw.huygens.timbuctoo.validation;

import nl.knaw.huygens.timbuctoo.storage.Storage;

public class RelationValidatorFactory {
  public final Storage storage;

  public RelationValidatorFactory(Storage storage) {
    this.storage = storage;
  }

  public RelationValidator createRelationValidator() {

    return new RelationValidator(new RelationTypeConformationValidator(storage), new RelationDuplicationValidator(storage));
  }
}
