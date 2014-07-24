package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RelationSearchResultCreatorTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testCreate() {
    // setup
    final SearchResult searchResultMock = mock(SearchResult.class);

    List<String> sourceIds = Lists.newArrayList();
    List<String> targetIds = Lists.newArrayList();
    List<String> relationTypeIds = Lists.newArrayList();
    String typeString = "test";

    String relId1 = "id1";
    String relId2 = "id2";
    Relation relation1 = createRelation(relId1);
    Relation relation2 = createRelation(relId2);
    Set<Relation> relations = Sets.newHashSet(relation1, relation2);

    RelationSearchResultCreator instance = new RelationSearchResultCreator() {
      @Override
      protected SearchResult createSearchResult() {
        return searchResultMock;
      }
    };

    // action
    SearchResult actualSearchResult = instance.create(relations, sourceIds, targetIds, relationTypeIds, typeString);

    // verify
    assertThat(actualSearchResult, equalTo(searchResultMock));
    verify(searchResultMock).setSourceIds(sourceIds);
    verify(searchResultMock).setTargetIds(targetIds);
    verify(searchResultMock).setIds((List<String>) argThat(containsInAnyOrder(relId1, relId2)));
    verify(searchResultMock).setRelationSearch(true);
    verify(searchResultMock).setRelationTypeIds(relationTypeIds);

  }

  private Relation createRelation(String id) {
    Relation relation = new Relation();
    relation.setId(id);
    // to satisfy Relation.equals
    relation.setSourceId("" + Math.random());

    return relation;
  }
}
