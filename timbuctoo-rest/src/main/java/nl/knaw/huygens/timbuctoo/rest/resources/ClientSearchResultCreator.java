package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

public interface ClientSearchResultCreator {

  ClientSearchResult create(SearchResult searchResult, int start, int rows);

}