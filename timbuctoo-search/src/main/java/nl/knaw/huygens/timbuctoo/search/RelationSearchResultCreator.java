package nl.knaw.huygens.timbuctoo.search;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import com.google.common.collect.Lists;

public class RelationSearchResultCreator {

  public SearchResult create(Set<Relation> filteredRelations, List<String> sourceIds, List<String> targetIds, List<String> relationTypeIds, String typeString) {

    SearchResult result = createSearchResult();
    result.setRelationSearch(true);
    result.setSearchType(typeString);
    result.setSourceIds(sourceIds);
    result.setTargetIds(targetIds);
    result.setRelationTypeIds(relationTypeIds);
    result.setIds(getRelationIds(filteredRelations));

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
