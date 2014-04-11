package nl.knaw.huygens.timbuctoo.index;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class IndexFacadeTest {

  private static final Class<ExplicitlyAnnotatedModel> BASE_TYPE = ExplicitlyAnnotatedModel.class;
  private static final Class<OtherIndexBaseType> OTHER_BASE_TYPE = OtherIndexBaseType.class;
  private static final String DEFAULT_ID = "id01234";
  private ScopeManager scopeManagerMock;
  private TypeRegistry typeRegistryMock;
  private IndexFacade instance;
  private StorageManager storageManagerMock;
  private Class<SubModel> type = SubModel.class;
  private IndexStatus indexStatusMock;

  @Before
  public void setUp() {
    indexStatusMock = mock(IndexStatus.class);
    storageManagerMock = mock(StorageManager.class);
    scopeManagerMock = mock(ScopeManager.class);
    typeRegistryMock = mock(TypeRegistry.class);
    doReturn(BASE_TYPE).when(typeRegistryMock).getBaseClass(type);
    instance = new IndexFacade(scopeManagerMock, typeRegistryMock, storageManagerMock) {
      @Override
      protected IndexStatus creatIndexStatus() {
        return indexStatusMock;
      }
    };
  }

  @Test
  public void testAddEntityInOneIndex() throws IndexException, IOException {
    // mock
    Scope scopeMock = mock(Scope.class);
    Index indexMock = mock(Index.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(BASE_TYPE), mock(type));
    List<ExplicitlyAnnotatedModel> filteredVariations = Lists.newArrayList();
    filteredVariations.add(mock(SubModel.class));

    // when
    when(storageManagerMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, BASE_TYPE)).thenReturn(indexMock);
    when(scopeMock.filter(variations)).thenReturn(filteredVariations);

    // action
    instance.addEntity(type, DEFAULT_ID);

    // verify
    InOrder inOrder = Mockito.inOrder(typeRegistryMock, storageManagerMock, scopeManagerMock, scopeMock, indexMock);
    inOrder.verify(typeRegistryMock).getBaseClass(type);
    inOrder.verify(storageManagerMock).getAllVariations(BASE_TYPE, DEFAULT_ID);
    inOrder.verify(scopeManagerMock).getAllScopes();
    inOrder.verify(scopeManagerMock).getIndexFor(scopeMock, BASE_TYPE);
    inOrder.verify(scopeMock).filter(variations);
    inOrder.verify(indexMock).add(filteredVariations);
  }

  @Test
  public void testAddEntityInMultipleIndexes() throws IndexException, IOException {
    // mock
    Scope scopeMock1 = mock(Scope.class);
    Scope scopeMock2 = mock(Scope.class);
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations1 = Lists.newArrayList();
    filteredVariations1.add(mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations2 = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class));

    // when
    when(storageManagerMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock1, scopeMock2));
    when(scopeManagerMock.getIndexFor(scopeMock1, BASE_TYPE)).thenReturn(indexMock1);
    when(scopeManagerMock.getIndexFor(scopeMock2, BASE_TYPE)).thenReturn(indexMock2);
    when(scopeMock1.filter(variations)).thenReturn(filteredVariations1);
    when(scopeMock2.filter(variations)).thenReturn(filteredVariations2);

    // action
    instance.addEntity(type, DEFAULT_ID);

    // verify
    verify(typeRegistryMock).getBaseClass(type);
    verify(storageManagerMock).getAllVariations(BASE_TYPE, DEFAULT_ID);
    verify(scopeManagerMock).getAllScopes();
    verify(scopeManagerMock).getIndexFor(scopeMock1, BASE_TYPE);
    verify(scopeManagerMock).getIndexFor(scopeMock2, BASE_TYPE);
    verify(scopeMock1).filter(variations);
    verify(scopeMock2).filter(variations);
    verify(indexMock1).add(filteredVariations1);
    verify(indexMock2).add(filteredVariations2);
  }

  @Test(expected = IndexException.class)
  public void testAddEntityStorageManagerThrowsAnIOException() throws IOException, IndexException {
    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;
    doThrow(IOException.class).when(storageManagerMock).getAllVariations(baseType, DEFAULT_ID);

    try {
      // action
      instance.addEntity(type, DEFAULT_ID);
    } finally {
      // verify
      verify(typeRegistryMock).getBaseClass(type);
      verify(storageManagerMock).getAllVariations(baseType, DEFAULT_ID);
      verifyZeroInteractions(scopeManagerMock);
    }
  }

  @Test(expected = IndexException.class)
  public void testAddIndexThrowsAnIndexException() throws IOException, IndexException {
    // mock
    Scope scopeMock = mock(Scope.class);
    Index indexMock = mock(Index.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations = Lists.newArrayList();
    filteredVariations.add(mock(SubModel.class));

    // when
    when(storageManagerMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, BASE_TYPE)).thenReturn(indexMock);
    when(scopeMock.filter(variations)).thenReturn(filteredVariations);
    doThrow(IndexException.class).when(indexMock).add(filteredVariations);

    try {
      // action
      instance.addEntity(type, DEFAULT_ID);
    } finally {
      // verify
      InOrder inOrder = Mockito.inOrder(typeRegistryMock, storageManagerMock, scopeManagerMock, scopeMock, indexMock);
      inOrder.verify(typeRegistryMock).getBaseClass(type);
      inOrder.verify(storageManagerMock).getAllVariations(BASE_TYPE, DEFAULT_ID);
      inOrder.verify(scopeManagerMock).getAllScopes();
      inOrder.verify(scopeManagerMock).getIndexFor(scopeMock, BASE_TYPE);
      inOrder.verify(scopeMock).filter(variations);
      inOrder.verify(indexMock).add(filteredVariations);
    }
  }

  @Test
  public void testUpdateEntity() throws IOException, IndexException {
    // mock
    Scope scopeMock = mock(Scope.class);
    Index indexMock = mock(Index.class);

    Class<? extends DomainEntity> type = SubModel.class;
    Class<? extends DomainEntity> baseType = ExplicitlyAnnotatedModel.class;
    List<DomainEntity> variations = Lists.newArrayList();
    SubModel model1 = mock(SubModel.class);
    variations.add(model1);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(model1);

    // when
    doReturn(variations).when(storageManagerMock).getAllVariations(baseType, DEFAULT_ID);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, baseType)).thenReturn(indexMock);
    when(scopeMock.filter(variations)).thenReturn(filteredVariations);

    // action
    instance.updateEntity(type, DEFAULT_ID);

    // verify
    verify(typeRegistryMock).getBaseClass(type);
    verify(storageManagerMock).getAllVariations(baseType, DEFAULT_ID);
    verify(scopeManagerMock).getAllScopes();
    verify(scopeManagerMock).getIndexFor(scopeMock, baseType);
    verify(scopeMock).filter(variations);
    verify(indexMock).update(filteredVariations);
  }

  @Test
  public void testDelete() throws IndexException {
    // setup
    Scope scopeMock = mock(Scope.class);
    Index indexMock = mock(Index.class);

    // when
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, BASE_TYPE)).thenReturn(indexMock);

    // action
    instance.deleteEntity(type, DEFAULT_ID);

    //verify
    InOrder inOrder = Mockito.inOrder(typeRegistryMock, scopeManagerMock, indexMock);
    inOrder.verify(typeRegistryMock).getBaseClass(type);
    inOrder.verify(scopeManagerMock).getAllScopes();
    inOrder.verify(scopeManagerMock).getIndexFor(scopeMock, BASE_TYPE);
    inOrder.verify(indexMock).deleteById(DEFAULT_ID);
  }

  @Test
  public void testDeleteMultipleScopes() throws IndexException {
    // setup
    Scope scopeMock1 = mock(Scope.class);
    Scope scopeMock2 = mock(Scope.class);
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock1, scopeMock2));
    when(scopeManagerMock.getIndexFor(scopeMock1, BASE_TYPE)).thenReturn(indexMock1);
    when(scopeManagerMock.getIndexFor(scopeMock2, BASE_TYPE)).thenReturn(indexMock2);

    // action
    instance.deleteEntity(type, DEFAULT_ID);

    //verify
    verify(typeRegistryMock).getBaseClass(type);
    verify(scopeManagerMock).getAllScopes();
    verify(scopeManagerMock).getIndexFor(scopeMock1, BASE_TYPE);
    verify(scopeManagerMock).getIndexFor(scopeMock2, BASE_TYPE);
    verify(indexMock1).deleteById(DEFAULT_ID);
    verify(indexMock2).deleteById(DEFAULT_ID);
  }

  @Test(expected = IndexException.class)
  public void testDeleteMultipleScopesFirstThrowsAnException() throws IndexException {
    // setup
    Scope scopeMock1 = mock(Scope.class);
    Scope scopeMock2 = mock(Scope.class);
    Index indexMock1 = mock(Index.class);

    // when
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock1, scopeMock2));
    when(scopeManagerMock.getIndexFor(scopeMock1, BASE_TYPE)).thenReturn(indexMock1);
    doThrow(IndexException.class).when(indexMock1).deleteById(DEFAULT_ID);

    try {
      // action
      instance.deleteEntity(type, DEFAULT_ID);
    } finally {
      //verify
      verify(typeRegistryMock).getBaseClass(type);
      verify(scopeManagerMock).getAllScopes();
      verify(scopeManagerMock).getIndexFor(scopeMock1, BASE_TYPE);
      verify(indexMock1).deleteById(DEFAULT_ID);
      verifyNoMoreInteractions(scopeManagerMock);
    }
  }

  @Test
  public void testDeleteEntities() throws IndexException {
    // setup
    Scope scopeMock = mock(Scope.class);
    Index indexMock = mock(Index.class);

    List<String> ids = Lists.newArrayList("id1", "id2", "id3");

    // when
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, BASE_TYPE)).thenReturn(indexMock);

    // action
    instance.deleteEntities(type, ids);

    // verify
    verify(typeRegistryMock).getBaseClass(type);
    verify(scopeManagerMock).getAllScopes();
    verify(scopeManagerMock).getIndexFor(scopeMock, BASE_TYPE);
    verify(indexMock).deleteById(ids);
  }

  @Test
  public void testDeleteAllEntities() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    List<Index> indexes = Lists.newArrayList(indexMock1, indexMock2);
    when(scopeManagerMock.getAllIndexes()).thenReturn(indexes);

    // action
    instance.deleteAllEntities();

    // verify
    verify(scopeManagerMock).getAllIndexes();
    verify(indexMock1).clear();
    verify(indexMock2).clear();
  }

  @Test(expected = IndexException.class)
  public void testDeleteAllEntitiesIndexClearThrowsAnIndexException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    List<Index> indexes = Lists.newArrayList(indexMock1, indexMock2);
    when(scopeManagerMock.getAllIndexes()).thenReturn(indexes);
    doThrow(IndexException.class).when(indexMock1).clear();

    try {
      // action
      instance.deleteAllEntities();
    } finally {
      // verify
      verify(scopeManagerMock).getAllIndexes();
      verify(indexMock1).clear();
      verifyZeroInteractions(indexMock2);
    }
  }

  @Test
  public void testGetStatus() throws IndexException {
    // setup
    Set<Class<? extends DomainEntity>> baseTypes = Sets.newHashSet();
    baseTypes.add(BASE_TYPE);
    baseTypes.add(OTHER_BASE_TYPE);

    Scope scopeMock1 = mock(Scope.class);
    Index scope1BaseTypeIndex = mock(Index.class);
    Index scope1OtherBaseTypeIndex = mock(Index.class);

    Scope scopeMock2 = mock(Scope.class);
    Index scope2BaseTypeIndex = mock(Index.class);
    Index scope2OtherBaseTypeIndex = mock(Index.class);

    // when
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock1, scopeMock2));

    doReturn(baseTypes).when(scopeMock1).getBaseEntityTypes();
    when(scopeManagerMock.getIndexFor(scopeMock1, BASE_TYPE)).thenReturn(scope1BaseTypeIndex);
    when(scopeManagerMock.getIndexFor(scopeMock1, OTHER_BASE_TYPE)).thenReturn(scope1OtherBaseTypeIndex);
    long itemCount1 = 42;
    when(scope1BaseTypeIndex.getCount()).thenReturn(itemCount1);
    long itemCount2 = 43;
    when(scope1OtherBaseTypeIndex.getCount()).thenReturn(itemCount2);

    doReturn(baseTypes).when(scopeMock2).getBaseEntityTypes();
    when(scopeManagerMock.getIndexFor(scopeMock2, BASE_TYPE)).thenReturn(scope2BaseTypeIndex);
    when(scopeManagerMock.getIndexFor(scopeMock2, OTHER_BASE_TYPE)).thenReturn(scope2OtherBaseTypeIndex);
    long itemCount3 = 44;
    when(scope2BaseTypeIndex.getCount()).thenReturn(itemCount3);
    long itemCount4 = 45;
    when(scope2OtherBaseTypeIndex.getCount()).thenReturn(itemCount4);

    // action
    IndexStatus actualIndexStatus = instance.getStatus();

    // verify
    verify(scopeManagerMock).getAllScopes();
    verify(scopeMock1).getBaseEntityTypes();
    verify(scopeManagerMock).getIndexFor(scopeMock1, BASE_TYPE);
    verify(scopeManagerMock).getIndexFor(scopeMock1, OTHER_BASE_TYPE);
    verify(scopeMock2).getBaseEntityTypes();
    verify(scopeManagerMock).getIndexFor(scopeMock2, BASE_TYPE);
    verify(scopeManagerMock).getIndexFor(scopeMock2, OTHER_BASE_TYPE);
    verify(scope1BaseTypeIndex).getCount();
    verify(scope1OtherBaseTypeIndex).getCount();
    verify(scope2BaseTypeIndex).getCount();
    verify(scope2OtherBaseTypeIndex).getCount();

    verify(indexStatusMock).addCount(scopeMock1, BASE_TYPE, itemCount1);
    verify(indexStatusMock).addCount(scopeMock1, OTHER_BASE_TYPE, itemCount2);
    verify(indexStatusMock).addCount(scopeMock2, BASE_TYPE, itemCount3);
    verify(indexStatusMock).addCount(scopeMock2, OTHER_BASE_TYPE, itemCount4);

    assertNotNull(actualIndexStatus);
  }

  @Test
  public void testGetStatusWhenIndexThrowsIndexException() throws IndexException {
    // setup
    Set<Class<? extends DomainEntity>> baseTypes = Sets.newHashSet();
    baseTypes.add(BASE_TYPE);
    baseTypes.add(OTHER_BASE_TYPE);

    Scope scopeMock = mock(Scope.class);
    Index indexMock = mock(Index.class);

    // when
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, BASE_TYPE)).thenReturn(indexMock);
    when(scopeManagerMock.getIndexFor(scopeMock, OTHER_BASE_TYPE)).thenReturn(indexMock);

    when(scopeMock.getBaseEntityTypes()).thenReturn(baseTypes);
    doThrow(IndexException.class).when(indexMock).getCount();

    // action
    IndexStatus actualStatus = instance.getStatus();

    // verify
    verify(scopeManagerMock).getAllScopes();
    verify(scopeMock).getBaseEntityTypes();
    verify(scopeManagerMock).getIndexFor(scopeMock, BASE_TYPE);
    verify(scopeManagerMock).getIndexFor(scopeMock, OTHER_BASE_TYPE);
    verify(indexMock, times(2)).getCount();

    verifyZeroInteractions(indexStatusMock);

    assertNotNull(actualStatus);
  }

  private static class OtherIndexBaseType extends DomainEntity {

    @Override
    public String getDisplayName() {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
