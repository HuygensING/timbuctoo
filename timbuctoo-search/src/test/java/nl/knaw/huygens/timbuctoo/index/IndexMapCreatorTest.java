package nl.knaw.huygens.timbuctoo.index;

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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import test.timbuctoo.index.model.Type1;
import test.timbuctoo.index.model.Type2;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class IndexMapCreatorTest {
  private IndexMapCreator instance;

  private IndexFactory indexFactoryMock;
  private IndexNameCreator indexNameCreatorMock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    indexNameCreatorMock = mock(IndexNameCreator.class);
    indexFactoryMock = mock(IndexFactory.class);

    instance = new IndexMapCreator(indexNameCreatorMock, indexFactoryMock);
  }

  @Test
  public void createIndexesForCreatesAMapOfIndexesWithEachBaseTypeOfTheVRE() {
    // setup
    Set<Class<? extends DomainEntity>> types = Sets.newHashSet();
    Class<Type1> type1 = Type1.class;
    types.add(type1);
    Class<Type2> type2 = Type2.class;
    types.add(type2);
    VRE vreMock = mock(VRE.class);
    when(vreMock.getEntityTypes()).thenReturn(types);

    String name1 = "type1";
    when(indexNameCreatorMock.getIndexNameFor(vreMock, type1)).thenReturn(name1);
    String name2 = "type2";
    when(indexNameCreatorMock.getIndexNameFor(vreMock, type2)).thenReturn(name2);

    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    Map<String, Index> expectedMap = Maps.newHashMap();
    expectedMap.put(name1, indexMock1);
    expectedMap.put(name2, indexMock2);

    when(indexFactoryMock.createIndexFor(type1, name1)).thenReturn(indexMock1);
    when(indexFactoryMock.createIndexFor(type2, name2)).thenReturn(indexMock2);

    // action
    Map<String, Index> actualMap = instance.createIndexesFor(vreMock);

    // verify
    assertThat(actualMap.entrySet(), equalTo(expectedMap.entrySet()));
  }

  @Test
  public void createIndexesForCreatesAnEmptyMapIfTheVREHasTypes() {
    VRE vreMock = mock(VRE.class);
    HashSet<Class<? extends DomainEntity>> emptyBaseTypeSet = Sets.newHashSet();
    when(vreMock.getBaseEntityTypes()).thenReturn(emptyBaseTypeSet);

    Map<String, Index> actualIndexMap = instance.createIndexesFor(vreMock);

    assertThat(actualIndexMap.entrySet(), is(empty()));
  }
}
