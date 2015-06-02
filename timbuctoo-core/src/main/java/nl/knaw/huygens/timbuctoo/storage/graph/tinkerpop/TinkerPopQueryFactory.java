package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQueryFactory;

class TinkerPopQueryFactory implements TimbuctooQueryFactory {

  public TinkerPopQuery newQuery() {
    return new TinkerPopQuery();
  }

}
