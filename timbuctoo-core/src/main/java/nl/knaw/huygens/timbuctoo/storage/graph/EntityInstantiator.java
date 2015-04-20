package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.Entity;

public class EntityInstantiator {

  public <T extends Entity> T createInstanceOf(Class<T> type) throws InstantiationException {
    try {
      return create(type);
    } catch (IllegalAccessException e) {
      throw new InstantiationException(e.getMessage());
    }
  }

  protected <T extends Entity> T create(Class<T> type) throws InstantiationException, IllegalAccessException {
    return type.newInstance();
  }
}
