package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQueryFactory;

import com.google.inject.Inject;

class TinkerPopQueryFactory implements TimbuctooQueryFactory {

  private PropertyBusinessRules businessRules;

  @Inject
  public TinkerPopQueryFactory() {
    this.businessRules = new PropertyBusinessRules();
  }

  @Override
  public TinkerPopQuery newQuery(Class<? extends Entity> type) {
    return new TinkerPopQuery(type, businessRules);
  }

}
