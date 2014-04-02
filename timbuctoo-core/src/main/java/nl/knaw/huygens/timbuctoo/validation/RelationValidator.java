package nl.knaw.huygens.timbuctoo.validation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;

public class RelationValidator implements Validator<Relation> {

  private RelationDuplicationValidator relationDuplicationValidator;

  public RelationValidator(RelationDuplicationValidator relationDuplicationValidator) {
    this.relationDuplicationValidator = relationDuplicationValidator;
  }

  @Override
  public void validate(Relation entityToValidate) throws ValidationException, IOException {
    relationDuplicationValidator.validate(entityToValidate);

  }

}
