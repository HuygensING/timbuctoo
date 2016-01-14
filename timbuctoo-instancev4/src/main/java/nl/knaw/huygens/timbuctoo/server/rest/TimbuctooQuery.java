package nl.knaw.huygens.timbuctoo.server.rest;

public class TimbuctooQuery {
  private final WwPersonSearchDescription description;

  public TimbuctooQuery(WwPersonSearchDescription description) {
    this.description = description;
  }

  public SearchResult execute() {
    return new SearchResult();
  }
}
