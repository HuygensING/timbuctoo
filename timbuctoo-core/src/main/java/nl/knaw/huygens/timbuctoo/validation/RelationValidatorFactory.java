package nl.knaw.huygens.timbuctoo.validation;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.storage.Storage;

public class RelationValidatorFactory {
  public final Storage storage;
  private final TypeRegistry typeRegistry;

  public RelationValidatorFactory(Storage storage, TypeRegistry typeRegistry) {
    this.storage = storage;
    this.typeRegistry = typeRegistry;
  }

  public RelationValidator createRelationValidator() {
    return new RelationValidator(new RelationTypeConformationValidator(storage), new RelationReferenceValidator(typeRegistry, storage), new RelationDuplicationValidator(storage));
  }
}
