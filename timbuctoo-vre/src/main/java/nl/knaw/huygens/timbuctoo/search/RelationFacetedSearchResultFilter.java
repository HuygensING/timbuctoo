package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo VRE
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID_FACET_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID_FACET_NAME;

import java.util.AbstractSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RelationFacetedSearchResultFilter implements FacetedSearchResultProcessor {

  private final CollectionConverter collectionConverter;
  private TargetSourceIdRelationPredicate predicate;

  public RelationFacetedSearchResultFilter(CollectionConverter collectionConverter, List<String> sourceSearchIds, List<String> targetSearchIds) {
    this.collectionConverter = collectionConverter;
    predicate = new TargetSourceIdRelationPredicate(targetSearchIds, sourceSearchIds);
  }

  @Override
  public void process(FacetedSearchResult facetedSearchResult) {
    FilterableSet<Map<String, Object>> filterableRawResults = collectionConverter.toFilterableSet(facetedSearchResult.getRawResults());

    facetedSearchResult.setRawResults(Lists.newArrayList(filterableRawResults.filter(predicate)));
  }

  private static final class TargetSourceIdRelationPredicate implements Predicate<Map<String, Object>> {
    private final Set<String> targetSearchIds;
    private final Set<String> sourceSearchIds;

    private TargetSourceIdRelationPredicate(List<String> targetSearchIds, List<String> sourceSearchIds) {
      this.targetSearchIds = createSet(targetSearchIds);
      this.sourceSearchIds = createSet(sourceSearchIds);
    }

    private AbstractSet<String> createSet(List<String> targetSearchIds) {
      return targetSearchIds != null ? Sets.newTreeSet(targetSearchIds) : Sets.<String> newHashSet();
    }

    @Override
    public boolean apply(Map<String, Object> input) {

      return sourceSearchIds.contains(getSourceIds(input)) && targetSearchIds.contains(getTargetIds(input));
    }

    private String getTargetIds(Map<String, Object> input) {
      return getFirstValueAsString(input, TARGET_ID_FACET_NAME);
    }

    @SuppressWarnings("unchecked")
    private String getFirstValueAsString(Map<String, Object> input, String fieldName) {
      return ((List<String>) input.get(fieldName)).get(0);
    }

    private String getSourceIds(Map<String, Object> input) {
      return getFirstValueAsString(input, SOURCE_ID_FACET_NAME);
    }
  }

  protected FacetedSearchResult createProcessedFacetedSearchResult() {
    return new FacetedSearchResult();
  }

}
