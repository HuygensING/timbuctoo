package nl.knaw.huygens.timbuctoo;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class AlreadyHasAPidException extends IllegalStateException {
  public <T extends DomainEntity> AlreadyHasAPidException(Class<T> type, T entity) {
    super(String.format("%s with %s already has a pid: %s", type.getSimpleName(), entity.getId(), entity.getPid()));
  }
}
