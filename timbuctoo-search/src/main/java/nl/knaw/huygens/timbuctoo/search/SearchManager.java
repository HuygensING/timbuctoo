package nl.knaw.huygens.timbuctoo.search;

import java.util.Set;

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.vre.Scope;

public interface SearchManager {

  Set<String> findSortableFields(Class<? extends DomainEntity> type);

  SearchResult search(Scope scope, Class<? extends DomainEntity> type, SearchParameters searchParameters) throws IndexException, NoSuchFacetException;

}