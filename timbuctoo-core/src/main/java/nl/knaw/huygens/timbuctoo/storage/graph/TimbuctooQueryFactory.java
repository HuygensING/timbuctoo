package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.Entity;

public interface TimbuctooQueryFactory {

  TimbuctooQuery newQuery(Class<? extends Entity> type);

}