package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;

public class EntityInstantiator {

  public <T extends Entity> T createInstanceOf(Class<T> type) throws InstantiationException, IllegalAccessException {
    return type.newInstance();
  }
}
