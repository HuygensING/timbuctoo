package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

public interface ClientSearchResultCreator {

  RegularClientSearchResult create(Class<? extends DomainEntity> type, SearchResult searchResult, int start, int rows);

}