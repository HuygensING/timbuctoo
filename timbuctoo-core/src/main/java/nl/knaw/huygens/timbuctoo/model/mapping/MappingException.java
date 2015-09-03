package nl.knaw.huygens.timbuctoo.model.mapping;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class MappingException extends Exception {
  public <T extends DomainEntity> MappingException(Class<?> type, Exception cause) {
    super(createMessage(type), cause);
  }

  private static String createMessage(Class<?> type) {
   return String.format("Could not map %s", type);
  }
}
