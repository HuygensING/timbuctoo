package nl.knaw.huygens.timbuctoo.validation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;

public class RelationValidator implements Validator<Relation> {

  private final RelationDuplicationValidator relationDuplicationValidator;
  private final RelationTypeConformationValidator relationTypeConformationValidator;
  private final RelationReferenceValidator relationReferenceValidator;

  public RelationValidator(RelationTypeConformationValidator relationTypeConformationValidator, RelationReferenceValidator relationFieldValidator, RelationDuplicationValidator relationDuplicationValidator) {
    this.relationTypeConformationValidator = relationTypeConformationValidator;
    this.relationDuplicationValidator = relationDuplicationValidator;
    this.relationReferenceValidator = relationFieldValidator;
  }

  @Override
  public void validate(Relation entityToValidate) throws ValidationException, IOException {
    relationTypeConformationValidator.validate(entityToValidate);
    relationReferenceValidator.validate(entityToValidate);
    relationDuplicationValidator.validate(entityToValidate);
  }

}
