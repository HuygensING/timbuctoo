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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class IndexFacadeTest {

  private static final Class<ExplicitlyAnnotatedModel> BASE_TYPE = ExplicitlyAnnotatedModel.class;
  private static final Class<OtherIndexBaseType> OTHER_BASE_TYPE = OtherIndexBaseType.class;
  private static final String DEFAULT_ID = "id01234";
  private IndexFacade instance;
  private Repository repositoryMock;
  private static final Class<SubModel> TYPE = SubModel.class;
  private IndexStatus indexStatusMock;
  private VREManager vreManagerMock;

  @Before
  public void setUp() {
    indexStatusMock = mock(IndexStatus.class);
    repositoryMock = mock(Repository.class);
    vreManagerMock = mock(VREManager.class);
    instance = new IndexFacade(repositoryMock, vreManagerMock) {
      @Override
      protected IndexStatus createIndexStatus() {
        return indexStatusMock;
      }
    };
  }

  @Test
  public void testAddEntityToOneVRE() throws IndexException, IOException {
    // mock
    VRE vreMock = mock(VRE.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(BASE_TYPE), mock(TYPE));

    // when
    when(repositoryMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));

    // action
    instance.addEntity(TYPE, DEFAULT_ID);

    // verify
    verify(vreMock).addToIndex(BASE_TYPE, variations);
  }

  @Test
  public void testAddEntityInMultipleVREs() throws IndexException, IOException {
    // mock
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));

    // when
    when(repositoryMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));

    // action
    instance.addEntity(TYPE, DEFAULT_ID);

    // verify
    verify(vreMock1).addToIndex(BASE_TYPE, variations);
    verify(vreMock2).addToIndex(BASE_TYPE, variations);
  }

  @Test
  public void testAddEntityStorageManagerReturnsEmptyList() throws IOException, IndexException {
    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;
    doReturn(Collections.emptyList()).when(repositoryMock).getAllVariations(baseType, DEFAULT_ID);

    try {
      // action
      instance.addEntity(type, DEFAULT_ID);
    } finally {
      // verify
      verify(repositoryMock).getAllVariations(baseType, DEFAULT_ID);
      verifyZeroInteractions(vreManagerMock);
    }
  }

  @Test(expected = IndexException.class)
  public void testAddToIndexThrowsAnIndexException() throws IOException, IndexException {
    // mock
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));

    // when
    when(repositoryMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));
    doThrow(IndexException.class).when(vreMock1).addToIndex(BASE_TYPE, variations);

    try {
      // action
      instance.addEntity(TYPE, DEFAULT_ID);
    } finally {
      // verify
      verify(vreMock1).addToIndex(BASE_TYPE, variations);
    }
  }

  @Test
  public void testUpdateEntity() throws IOException, IndexException {
    // mock
    VRE vreMock = mock(VRE.class);
    List<DomainEntity> variations = Lists.newArrayList();
    SubModel model1 = mock(SubModel.class);
    variations.add(model1);

    // when
    doReturn(variations).when(repositoryMock).getAllVariations(BASE_TYPE, DEFAULT_ID);
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));

    // action
    instance.updateEntity(TYPE, DEFAULT_ID);

    // verify
    verify(vreMock).updateIndex(BASE_TYPE, variations);
  }

  @Test
  public void testDelete() throws IndexException {
    // setup
    VRE vreMock = mock(VRE.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));

    // action
    instance.deleteEntity(TYPE, DEFAULT_ID);

    //verify
    verify(vreMock).deleteFromIndex(TYPE, DEFAULT_ID);
  }

  @Test
  public void testDeleteMultipleScopes() throws IndexException {
    // setup
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));

    // action
    instance.deleteEntity(TYPE, DEFAULT_ID);

    //verify
    verify(vreMock1).deleteFromIndex(TYPE, DEFAULT_ID);
    verify(vreMock2).deleteFromIndex(TYPE, DEFAULT_ID);
  }

  @Test(expected = IndexException.class)
  public void testDeleteMultipleScopesFirstThrowsAnException() throws IndexException {
    // setup
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));
    doThrow(IndexException.class).when(vreMock1).deleteFromIndex(TYPE, DEFAULT_ID);

    try {
      // action
      instance.deleteEntity(TYPE, DEFAULT_ID);
    } finally {
      //verify
      verify(vreMock1).deleteFromIndex(TYPE, DEFAULT_ID);
      verifyZeroInteractions(vreMock2);
    }
  }

  @Test
  public void testDeleteEntities() throws IndexException {
    // setup
    VRE vreMock = mock(VRE.class);
    List<String> ids = Lists.newArrayList("id1", "id2", "id3");

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));

    // action
    instance.deleteEntities(TYPE, ids);

    // verify
    verify(vreMock).deleteFromIndex(TYPE, ids);
  }

  @Test
  public void testDeleteAllEntities() throws IndexException {
    // setup
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));

    // action
    instance.deleteAllEntities();

    // verify
    verify(vreMock1).clearIndexes();
    verify(vreMock2).clearIndexes();
  }

  @Test(expected = IndexException.class)
  public void testDeleteAllEntitiesVREClearIndexesThrowsAnIndexException() throws IndexException {
    // setup
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));
    doThrow(IndexException.class).when(vreMock1).clearIndexes();

    try {
      // action
      instance.deleteAllEntities();
    } finally {
      // verify
      verify(vreMock1).clearIndexes();
      verifyZeroInteractions(vreMock2);
    }
  }

  @Test
  public void testGetStatus() throws IndexException {
    // setup
    Set<Class<? extends DomainEntity>> baseTypes = Sets.newHashSet();
    baseTypes.add(BASE_TYPE);
    baseTypes.add(OTHER_BASE_TYPE);

    VRE vreMock1 = mock(VRE.class);
    Index vre1BaseTypeIndex = mock(Index.class);
    Index vre1OtherBaseTypeIndex = mock(Index.class);

    VRE vreMock2 = mock(VRE.class);
    Index vre2BaseTypeIndex = mock(Index.class);
    Index vre2OtherBaseTypeIndex = mock(Index.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));

    doReturn(baseTypes).when(vreMock1).getBaseEntityTypes();
    when(vreManagerMock.getIndexFor(vreMock1, BASE_TYPE)).thenReturn(vre1BaseTypeIndex);
    when(vreManagerMock.getIndexFor(vreMock1, OTHER_BASE_TYPE)).thenReturn(vre1OtherBaseTypeIndex);
    long itemCount1 = 42;
    when(vre1BaseTypeIndex.getCount()).thenReturn(itemCount1);
    long itemCount2 = 43;
    when(vre1OtherBaseTypeIndex.getCount()).thenReturn(itemCount2);

    doReturn(baseTypes).when(vreMock2).getBaseEntityTypes();
    when(vreManagerMock.getIndexFor(vreMock2, BASE_TYPE)).thenReturn(vre2BaseTypeIndex);
    when(vreManagerMock.getIndexFor(vreMock2, OTHER_BASE_TYPE)).thenReturn(vre2OtherBaseTypeIndex);
    long itemCount3 = 44;
    when(vre2BaseTypeIndex.getCount()).thenReturn(itemCount3);
    long itemCount4 = 45;
    when(vre2OtherBaseTypeIndex.getCount()).thenReturn(itemCount4);

    // action
    IndexStatus actualIndexStatus = instance.getStatus();

    // verify
    verify(indexStatusMock).addCount(vreMock1, BASE_TYPE, itemCount1);
    verify(indexStatusMock).addCount(vreMock1, OTHER_BASE_TYPE, itemCount2);
    verify(indexStatusMock).addCount(vreMock2, BASE_TYPE, itemCount3);
    verify(indexStatusMock).addCount(vreMock2, OTHER_BASE_TYPE, itemCount4);

    assertNotNull(actualIndexStatus);
  }

  @Test
  public void testGetStatusWhenIndexThrowsIndexException() throws IndexException {
    // setup
    Set<Class<? extends DomainEntity>> baseTypes = Sets.newHashSet();
    baseTypes.add(BASE_TYPE);
    baseTypes.add(OTHER_BASE_TYPE);

    VRE vreMock = mock(VRE.class);
    Index indexMock = mock(Index.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));
    when(vreManagerMock.getIndexFor(vreMock, BASE_TYPE)).thenReturn(indexMock);
    when(vreManagerMock.getIndexFor(vreMock, OTHER_BASE_TYPE)).thenReturn(indexMock);

    when(vreMock.getBaseEntityTypes()).thenReturn(baseTypes);
    doThrow(IndexException.class).when(indexMock).getCount();

    // action
    IndexStatus actualStatus = instance.getStatus();

    // verify
    verify(indexMock, times(2)).getCount();

    verifyZeroInteractions(indexStatusMock);

    assertNotNull(actualStatus);
  }

  @Test
  public void testCommitAll() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    List<Index> indexes = Lists.newArrayList(indexMock1, indexMock2);

    // when
    when(vreManagerMock.getAllIndexes()).thenReturn(indexes);

    // action
    instance.commitAll();

    // verify
    verify(indexMock1).commit();
    verify(indexMock2).commit();
  }

  @Test(expected = IndexException.class)
  public void testCommitAllFirstIndexThrowsAnIndexException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    List<Index> indexes = Lists.newArrayList(indexMock1, indexMock2);

    // when
    when(vreManagerMock.getAllIndexes()).thenReturn(indexes);
    doThrow(IndexException.class).when(indexMock1).commit();

    try {
      // action
      instance.commitAll();
    } finally {
      // verify
      verify(indexMock1).commit();
      verifyZeroInteractions(indexMock2);
    }
  }

  @Test
  public void testClose() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    when(vreManagerMock.getAllIndexes()).thenReturn(Lists.newArrayList(indexMock1, indexMock2));

    // action
    instance.close();

    // verify
    verify(indexMock1).close();
    verify(indexMock2).close();
  }

  @Test
  public void testCloseFirstThrowsIndexException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    when(vreManagerMock.getAllIndexes()).thenReturn(Lists.newArrayList(indexMock1, indexMock2));
    doThrow(IndexException.class).when(indexMock1).close();

    // action
    instance.close();

    // verify
    verify(indexMock1).close();
    verify(indexMock2).close();
  }

  private static class OtherIndexBaseType extends DomainEntity {

    @Override
    public String getDisplayName() {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
