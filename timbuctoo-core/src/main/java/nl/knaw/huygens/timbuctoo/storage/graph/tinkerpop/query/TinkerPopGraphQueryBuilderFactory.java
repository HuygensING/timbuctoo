package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.tinkerpop.blueprints.Graph;

public class TinkerPopGraphQueryBuilderFactory {
  private Graph db;

  public TinkerPopGraphQueryBuilderFactory(Graph db) {
    this.db = db;
  }

  public TinkerPopGraphQueryBuilder newQueryBuilder(Class<? extends Entity> type) {
    return new TinkerPopGraphQueryBuilder(type, db);
  }
}
