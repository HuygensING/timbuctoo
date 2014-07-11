package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.rest.resources.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ClientSearchResultCreatorTest {

  protected static final String NEXT_LINK = "http://www.test.com/next";
  protected static final String PREV_LINK = "http://www.test.com/prev";
  protected static final ArrayList<String> ID_LIST_WITH_TEN_IDS = Lists.newArrayList("id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10");
  protected static final int NUMBER_OF_RESULTS_FOUND = ID_LIST_WITH_TEN_IDS.size();
  protected static final String QUERY_ID = "queryId";
  protected static final HashSet<String> SORTABLE_FIELDS = Sets.newHashSet();
  protected Repository repositoryMock;
  protected SortableFieldFinder sortableFieldFinderMock;
  protected String nullPrevLink;
  protected String nullNextLink;
  protected HATEOASURICreator hateoasURICreatorMock;

  public void initializeRepository() {
    repositoryMock = mock(Repository.class);
  }

  public void initializeHATEOASURICreator() {
    hateoasURICreatorMock = mock(HATEOASURICreator.class);
  }

  public void initilizeSortableFieldFinder() {
    sortableFieldFinderMock = mock(SortableFieldFinder.class);
  }

  protected <T extends DomainEntity> List<T> setupRepository(Class<T> type, List<String> idList) throws InstantiationException, IllegalAccessException {
    List<T> domainEntities = Lists.newArrayList();

    for (String id : idList) {

      final T domainEntity = type.newInstance();
      domainEntities.add(domainEntity);
      doReturn(domainEntity).when(repositoryMock).getEntity(type, id);
    }

    return domainEntities;

  }

}