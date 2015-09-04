package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.Entity;

import java.util.UUID;

public class IdGenerator {

  // TODO find a solution for support legacy id's 
  public String nextIdFor(Class<? extends Entity> type) {
    return UUID.randomUUID().toString();
  }
}
