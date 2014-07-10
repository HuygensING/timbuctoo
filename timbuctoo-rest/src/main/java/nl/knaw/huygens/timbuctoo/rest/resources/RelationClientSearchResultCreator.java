package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

public class RelationClientSearchResultCreator implements ClientSearchResultCreator {

  @Override
  public <T extends DomainEntity> RegularClientSearchResult create(Class<T> type, SearchResult searchResult, int start, int rows) {
    // TODO Auto-generated method stub
    return null;
  }

}
