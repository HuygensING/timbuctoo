package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResultDTO;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import java.util.List;
import java.util.Map;

public class IndexRelationSearchResultMapper extends RelationSearchResultMapper {

  private final RelationDTOListFactory relationDTOListFactory;

  @Inject
  public IndexRelationSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, RelationDTOListFactory relationDTOListFactory, VRECollection vreCollection, RangeHelper rangeHelper) {
    super(repository, sortableFieldFinder, hateoasURICreator, relationDTOListFactory, vreCollection, rangeHelper);
    this.relationDTOListFactory = relationDTOListFactory;
  }

  @Override
  public <T extends DomainEntity> SearchResultDTO create(Class<T> type, SearchResult searchResult, int start, int rows, String version) {
    RelationSearchResultDTO dto = new RelationSearchResultDTO();

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
    dto.setIds(ids);
    dto.setNumFound(numFound);
    dto.setSourceType(searchResult.getSourceType());
    dto.setTargetType(searchResult.getTargetType());

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
}
