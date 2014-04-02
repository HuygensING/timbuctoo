package nl.knaw.huygens.timbuctoo.validation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;

import com.google.inject.Inject;

public class RelationDuplicationValidator implements Validator<Relation> {

  private final Storage storage;

  @Inject
  public RelationDuplicationValidator(Storage storage) {
    this.storage = storage;
  }

  @Override
  public void validate(Relation entityToValidate) throws ValidationException, IOException {
    Relation example = new Relation();
    example.setSourceId(entityToValidate.getSourceId());
    example.setTargetId(entityToValidate.getTargetId());
    example.setTypeId(entityToValidate.getTypeId());

    Relation inverseExample = new Relation();
    inverseExample.setSourceId(entityToValidate.getTargetId());
    inverseExample.setTargetId(entityToValidate.getSourceId());
    inverseExample.setTypeId(entityToValidate.getTypeId());

    if (storage.findItem(Relation.class, example) != null) {
      throw new DuplicateException();
    }
    if (storage.findItem(Relation.class, inverseExample) != null) {
      throw new DuplicateException();
    }
  }
}
