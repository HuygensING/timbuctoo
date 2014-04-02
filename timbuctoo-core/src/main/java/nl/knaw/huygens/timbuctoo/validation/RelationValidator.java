package nl.knaw.huygens.timbuctoo.validation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;

public class RelationValidator implements Validator<Relation> {

  private RelationDuplicationValidator relationDuplicationValidator;
  private RelationTypeConformationValidator relationTypeConformationValidator;

  public RelationValidator(RelationTypeConformationValidator relationTypeConformationValidator, RelationDuplicationValidator relationDuplicationValidator) {
    this.relationTypeConformationValidator = relationTypeConformationValidator;
    this.relationDuplicationValidator = relationDuplicationValidator;
  }

  @Override
  public void validate(Relation entityToValidate) throws ValidationException, IOException {
    relationTypeConformationValidator.validate(entityToValidate);
    relationDuplicationValidator.validate(entityToValidate);
  }

}
