package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.PredicateHandler;

public abstract class PredicateData {
  private final String uri;

  protected PredicateData(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  public abstract void handle(PredicateHandler handler);
}
