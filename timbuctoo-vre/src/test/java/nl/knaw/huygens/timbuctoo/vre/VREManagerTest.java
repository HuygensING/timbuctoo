package nl.knaw.huygens.timbuctoo.vre;

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

import static nl.knaw.huygens.timbuctoo.vre.VREManagerMatcher.matchesVREManager;
import static nl.knaw.huygens.timbuctoo.vre.VREMockBuilder.newVRE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexNameCreator;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VREManagerTest {

  @Mock
  private Map<String, Index> indexMapMock;

  @Mock
  private Map<String, VRE> vreMapMock;

  private IndexNameCreator indexNameCreatorMock;

  private VREManager instance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    indexNameCreatorMock = mock(IndexNameCreator.class);
    instance = new VREManager(vreMapMock);
  }

  @Test
  public void testGetIndexFor() {
    // mock
    VRE vreMock = mock(VRE.class);

    Class<ExplicitlyAnnotatedModel> type = ExplicitlyAnnotatedModel.class;

    Index indexMock = mock(Index.class);

    // when
    when(vreMock.getIndexForType(type)).thenReturn(indexMock);

    // action
    Index actualIndex = instance.getIndexFor(vreMock, type);

    // verify
    verify(vreMock).getIndexForType(type);
    assertThat(actualIndex, equalTo(indexMock));
  }

  @Test
  public void getVREByIdShouldReturnTheVREWhenFound() {
    // setup
    VRE vreMock = mock(VRE.class);
    String vreId = "vreId";

    when(vreMapMock.get(vreId)).thenReturn(vreMock);

    // action
    VRE actualVRE = instance.getVREById(vreId);

    //verify
    assertThat(actualVRE, equalTo(vreMock));
  }

  @Test
  public void getVREByIdShouldReturnNullWhenVRENotFound() {
    // setup
    String vreId = "vreId";

    // action
    VRE actualVRE = instance.getVREById(vreId);

    //verify
    assertThat(actualVRE, equalTo(null));
  }

  @Test
  public void getAllIndexesShouldReturnTheIndexesOfAllTheVREs() {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);
    Index indexMock3 = mock(Index.class);
    Index indexMock4 = mock(Index.class);

    VRE vreMock1 = newVRE().withIndexes(indexMock1, indexMock2).create();
    VRE vreMock2 = newVRE().withIndexes(indexMock3, indexMock4).create();

    when(vreMapMock.values()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));

    // action
    Collection<Index> actualIndexes = instance.getAllIndexes();

    // verify
    verify(vreMock1).getIndexes();
    verify(vreMock2).getIndexes();
    assertThat(actualIndexes, contains(new Index[] { indexMock1, indexMock2, indexMock3, indexMock4 }));
  }

  @Test
  public void createInstanceCreatesAnInstanceWithGeneratedVREAndIndexMaps() {
    // setup
    String vreName1 = "VRE1";
    String vreName2 = "VRE2";

    Map<String, VRE> vres = Maps.newHashMap();

    VRE vre1 = newVRE().withName(vreName1).create();
    vres.put(vreName1, vre1);
    VRE vre2 = newVRE().withName(vreName2).create();
    vres.put(vreName2, vre2);

    VREManager expectedVREManager = new VREManager(vres);
    IndexFactory indexFactory = mock(IndexFactory.class);

    // action
    VREManager actualVREManager = VREManager.createInstance(//
        Lists.newArrayList(vre1, vre2), //
        indexNameCreatorMock, //
        indexFactory);

    //verify
    assertThat(actualVREManager, matchesVREManager(expectedVREManager));
    verify(vre1).initIndexes(indexFactory);
    verify(vre2).initIndexes(indexFactory);
  }
}
