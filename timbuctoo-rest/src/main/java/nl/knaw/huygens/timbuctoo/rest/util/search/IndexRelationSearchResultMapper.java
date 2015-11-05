package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.facetedsearch.model.DefaultFacet;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.FacetOption;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTOV2_1;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResultDTO;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import nl.knaw.huygens.timbuctoo.vre.VREException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexRelationSearchResultMapper extends RelationSearchResultMapper {

  private final RelationDTOListFactory relationDTOListFactory;
  private final FullTextSearchFieldFinder fullTextSearchFieldFinder;
  private final TypeRegistry registry;

  @Inject
  public IndexRelationSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, RelationDTOListFactory relationDTOListFactory, VRECollection vreCollection, RangeHelper rangeHelper, FullTextSearchFieldFinder fullTextSearchFieldFinder, TypeRegistry registry) {
    super(repository, sortableFieldFinder, hateoasURICreator, relationDTOListFactory, vreCollection, rangeHelper);
    this.relationDTOListFactory = relationDTOListFactory;
    this.fullTextSearchFieldFinder = fullTextSearchFieldFinder;
    this.registry = registry;
  }

  @Override
  public <T extends DomainEntity> SearchResultDTO create(Class<T> type, SearchResult searchResult, int start, int rows, String version) {
    RelationSearchResultDTOV2_1 dto = new RelationSearchResultDTOV2_1();

    String queryId = searchResult.getId();
    VRE vre = vreCollection.getVREById(searchResult.getVreId());
    List<String> ids = getIds(searchResult);
    int numFound = ids.size();
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - normalizedStart);
    int end = normalizedStart + normalizedRows;

    List<String> idsToRetrieve = ids.subList(normalizedStart, end);
    List<Map<String, Object>> rawData = null;
    try {
      rawData = vre.getRawDataFor(type, idsToRetrieve, searchResult.getSort());
    } catch (NotInScopeException | SearchException e) {
      throw new RuntimeException(e); // FIXME: Hack to inform the client the search went wrong, and not change the API
    }

    dto.setRows(normalizedRows);
    dto.setStart(normalizedStart);
    dto.setNumFound(numFound);
    dto.setFullTextSearchFields(getFullTextSearchFieldsOfTarget(searchResult));

    List<Facet> facets = Lists.newArrayList(searchResult.getFacets());
    try {
      facets.add(createRelationFacet(vre, searchResult));
    } catch (VREException e) {
      throw new RuntimeException(e); // FIXME: Hack to inform the client the search went wrong, and not change the API
    }
    dto.setFacets(facets);

    dto.setTerm(searchResult.getTerm());

    try {
      dto.setRefs(relationDTOListFactory.create(vre, type, rawData));
    }
    catch(SearchResultCreationException e){
      throw new RuntimeException(e);
    }
    dto.setSortableFields(sortableFieldFinder.findFields(type));
    setNextLink(normalizedStart, rows, dto, numFound, end, queryId, version);
    setPreviousLink(normalizedStart, rows, dto, queryId, version);

    return dto;
  }

  private Facet createRelationFacet(VRE vre, SearchResult searchResult) throws VREException {
    Class<? extends DomainEntity> sourceType = registry.getDomainEntityType(searchResult.getSourceType());
    Class<? extends DomainEntity> targetType = registry.getDomainEntityType(searchResult.getTargetType());

    List<String> relationTypeNamesBetween = vre.getRelationTypeNamesBetween(sourceType, targetType);

    DefaultFacet facet = new DefaultFacet(RelationSearchParametersConverter.RELATION_FACET, RelationSearchParametersConverter.RELATION_FACET);

    for (String name : relationTypeNamesBetween) {
      facet.addOption(new FacetOption(name, 0));
    }


    return facet;
  }

  private Set<String> getFullTextSearchFieldsOfTarget(SearchResult searchResult) {
    Class<? extends DomainEntity> targetType = registry.getDomainEntityType(searchResult.getTargetType());
    return fullTextSearchFieldFinder.findFields(targetType);
  }
}
