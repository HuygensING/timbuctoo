package nl.knaw.huygens.timbuctoo.validation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.Storage;

/**
 * Checks if the relation conforms to the type.
 */
public class RelationTypeConformationValidator implements Validator<Relation> {

  private final Storage storageMock;

  public RelationTypeConformationValidator(Storage storage) {
    this.storageMock = storage;
  }

  @Override
  public void validate(Relation entityToValidate) throws ValidationException, IOException {
    String relationTypeId = entityToValidate.getTypeId();

    RelationType relationType = storageMock.getItem(RelationType.class, relationTypeId);

    if (relationType == null) {
      throw new ValidationException("RelationType with id " + relationTypeId + " does not exist");
    }

    if (!entityToValidate.conformsToRelationType(relationType)) {
      throw new ValidationException("Relation is not conform the RelationType with id " + relationTypeId);
    }
  }
}
