package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.Entity;

public class TimbuctooQueryFactory {

  public TimbuctooQuery newQuery(Class<? extends Entity> type) {
    return new TimbuctooQuery(type);
  }

}
