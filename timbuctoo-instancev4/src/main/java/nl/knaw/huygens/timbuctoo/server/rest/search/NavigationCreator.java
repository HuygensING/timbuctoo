package nl.knaw.huygens.timbuctoo.server.rest.search;

import nl.knaw.huygens.timbuctoo.server.SearchConfig;

import java.util.UUID;

class NavigationCreator {
  private SearchConfig searchConfig;

  public NavigationCreator(SearchConfig searchConfig) {
    this.searchConfig = searchConfig;
  }

  public void next(SearchResponseV2_1 searchResponse, int rows, int currentStart, int numFound, UUID id) {
    int nextStart = currentStart + rows;
    if (numFound > nextStart) {
      searchResponse
        .setNext(String.format("%s/v2.1/search/%s?start=%d&rows=%d", searchConfig.getBaseUri(), id, nextStart, rows));
    }
  }
}
