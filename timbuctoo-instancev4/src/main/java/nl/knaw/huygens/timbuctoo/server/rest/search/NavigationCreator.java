package nl.knaw.huygens.timbuctoo.server.rest.search;

import nl.knaw.huygens.timbuctoo.server.SearchConfig;

import javax.ws.rs.core.UriBuilder;
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
        .setNext(createUri(id, nextStart, rows));
    }
  }

  public void prev(SearchResponseV2_1 searchResponse, int rows, int currentStart, int numFound, UUID id) {
    if (currentStart > 0) {
      int prevStart = Math.max(0, currentStart - rows);
      searchResponse.setPrev(createUri(id, prevStart, rows));
    }
  }

  private String createUri(UUID id, int start, int rows) {

    return UriBuilder.fromUri(searchConfig.getBaseUri()).path("/v2.1/search/{id}")
                     .queryParam("start", start)
                     .queryParam("rows", rows)
                     .build(id)
                     .toString();
  }
}
