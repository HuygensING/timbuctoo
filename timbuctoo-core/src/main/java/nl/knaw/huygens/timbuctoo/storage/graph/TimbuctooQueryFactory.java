package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.inject.Inject;

public class TimbuctooQueryFactory {

  private PropertyBusinessRules businessRules;

  @Inject
  public TimbuctooQueryFactory() {
    this.businessRules = new PropertyBusinessRules();
  }

  public TimbuctooQuery newQuery(Class<? extends Entity> type) {
    return new TimbuctooQuery(type, businessRules);
  }

}
