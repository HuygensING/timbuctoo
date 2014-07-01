package nl.knaw.huygens.timbuctoo.search;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;

@Singleton
public class RelationSearchResultCreator {

  private static Logger LOG = LoggerFactory.getLogger(RelationSearchResultCreator.class);

  public SearchResult create(Set<Relation> filteredRelations, List<String> sourceIds, List<String> targetIds, List<String> relationTypeIds, String typeString) {

    SearchResult result = createSearchResult();
    result.setRelationSearch(true);
    result.setSearchType(typeString);
    result.setSourceIds(sourceIds);
    result.setTargetIds(targetIds);
    result.setRelationTypeIds(relationTypeIds);

    StopWatch getRelationIdsStopWatch = new StopWatch();
    getRelationIdsStopWatch.start();

    result.setIds(getRelationIds(filteredRelations));

    getRelationIdsStopWatch.stop();
    LOG.info(String.format("%s: %.3f seconds", "getRelationIds", (double) getRelationIdsStopWatch.getTime() / 1000));

    return result;
  }

  private List<String> getRelationIds(Set<Relation> filteredRelations) {
    List<String> relationIds = Lists.newArrayList();

    for (Relation relation : filteredRelations) {
      relationIds.add(relation.getId());
    }

    return relationIds;
  }

  protected SearchResult createSearchResult() {
    return new SearchResult();
  }

}
