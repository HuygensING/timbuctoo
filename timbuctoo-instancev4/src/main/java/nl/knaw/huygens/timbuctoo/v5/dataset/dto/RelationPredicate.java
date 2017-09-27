package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.PredicateHandler;

import java.util.List;

public class RelationPredicate extends PredicateData {
  private final String relatedUri;
  private final List<String> types;

  public RelationPredicate(String uri, String relatedUri, List<String> types) {
    super(uri);
    this.relatedUri = relatedUri;
    this.types = types;
  }

  @Override
  public void handle(PredicateHandler handler) {
    handler.onRelation(relatedUri, types);
  }
}
