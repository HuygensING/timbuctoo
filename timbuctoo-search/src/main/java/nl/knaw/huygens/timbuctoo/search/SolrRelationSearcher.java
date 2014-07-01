package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.SearchException;
import nl.knaw.huygens.timbuctoo.index.SearchValidationException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.converters.RelationFacetedSearchResultConverter;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

public class SolrRelationSearcher extends RelationSearcher {

  private final VREManager vreManager;
  private final RelationSearchParametersConverter relationSearchParametersConverter;
  private final TypeRegistry typeRegistry;
  private final RelationFacetedSearchResultConverter searchResultConverter;

  public SolrRelationSearcher(Repository repository, VREManager vreManager, RelationSearchParametersConverter relationSearchParametersConverter, TypeRegistry typeRegistry,
      RelationFacetedSearchResultConverter searchResultConverter) {
    super(repository);
    this.vreManager = vreManager;
    this.relationSearchParametersConverter = relationSearchParametersConverter;
    this.typeRegistry = typeRegistry;
    this.searchResultConverter = searchResultConverter;
  }

  @Override
  public SearchResult search(VRE vre, RelationSearchParameters relationSearchParameters) throws SearchException, SearchValidationException {
    getRelationTypeIds(vre, relationSearchParameters);

    SearchParametersV1 searchParametersV1 = relationSearchParametersConverter.toSearchParamtersV1(relationSearchParameters);

    final String typeString = relationSearchParameters.getTypeString();
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);

    Index index = vreManager.getIndexFor(vre, type);

    FacetedSearchResult facetedSearchResult = null;

    facetedSearchResult = index.search(searchParametersV1);

    SearchResult searchResult = searchResultConverter.convert(typeString, facetedSearchResult);

    return searchResult;
  }

  private void getRelationTypeIds(VRE vre, RelationSearchParameters relationSearchParameters) {
    if (relationSearchParameters.getRelationTypeIds() == null || relationSearchParameters.getRelationTypeIds().isEmpty()) {
      relationSearchParameters.setRelationTypeIds(repository.getRelationTypeIdsByName(vre.getReceptionNames()));
    }
  }
}
