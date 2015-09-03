package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class NoSuchRelationException extends StorageException {
  private static final long serialVersionUID = 1L;

  public NoSuchRelationException(Class<? extends Relation> type, String id) {
    this("\"%s\" with \"%s\" does not exist.", TypeNames.getExternalName(type), id);
  }

  public NoSuchRelationException(String format, Object... args) {
    super(String.format(format, args));
  }

}
