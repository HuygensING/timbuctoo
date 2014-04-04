package nl.knaw.huygens.timbuctoo.validation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;

public class RelationReferenceValidator implements Validator<Relation> {

  private final TypeRegistry typeRegistry;
  private final Storage storage;

  public RelationReferenceValidator(TypeRegistry typeRegistry, Storage storage) {
    this.typeRegistry = typeRegistry;
    this.storage = storage;
  }

  @Override
  public void validate(Relation entityToValidate) throws ValidationException, IOException {
    String sourceType = entityToValidate.getSourceType();
    String sourceId = entityToValidate.getSourceId();

    if (!entityExists(sourceType, sourceId)) {
      throw new ValidationException(createValidationMessage(sourceType, sourceId));
    }

    String targetType = entityToValidate.getTargetType();
    String targetId = entityToValidate.getTargetId();
    if (!entityExists(targetType, targetId)) {
      throw new ValidationException(createValidationMessage(targetType, targetId));
    }

  }

  private String createValidationMessage(String type, String id) {
    return String.format("Entity of type %s with id %s does not exist.", type, id);
  }

  private boolean entityExists(String typeString, String id) throws IOException {
    Class<? extends Entity> sourceType = typeRegistry.getTypeForIName(typeString);
    return storage.getItem(sourceType, id) != null;
  }

}
