package nl.knaw.huygens.timbuctoo.server.rest;

import java.util.List;

public class SearchResult {

  private List<EntityRef> refs;

  public SearchResult(List<EntityRef> refs) {
    this.refs = refs;
  }

  public List<EntityRef> getRefs() {
    return refs;
  }
}
