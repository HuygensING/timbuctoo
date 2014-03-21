package nl.knaw.huygens.timbuctoo.index;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

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

public class IndexFacadeTest {

  private static final String DEFAULT_ID = "id01234";
  private ScopeManager scopeManagerMock;
  private TypeRegistry typeRegistryMock;
  private IndexFacade instance;
  private StorageManager storageManagerMock;

  @Before
  public void setUp() {
    storageManagerMock = mock(StorageManager.class);
    scopeManagerMock = mock(ScopeManager.class);
    typeRegistryMock = mock(TypeRegistry.class);
    instance = new IndexFacade(scopeManagerMock, typeRegistryMock, storageManagerMock);
  }

  @Test
  public void testAddEntityInOneIndex() throws IndexException, IOException {
    // mock
    Scope scopeMock = mock(Scope.class);
    Index indexMock = mock(Index.class);

    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;
    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations = Lists.newArrayList();
    filteredVariations.add(mock(SubModel.class));

    // when
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);
    when(storageManagerMock.getAllVariations(baseType, DEFAULT_ID)).thenReturn(variations);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, baseType)).thenReturn(indexMock);
    when(scopeMock.filter(variations)).thenReturn(filteredVariations);

    // action
    instance.addEntity(type, DEFAULT_ID);

    // verify
    InOrder inOrder = Mockito.inOrder(typeRegistryMock, storageManagerMock, scopeManagerMock, scopeMock, indexMock);
    inOrder.verify(typeRegistryMock).getBaseClass(type);
    inOrder.verify(storageManagerMock).getAllVariations(baseType, DEFAULT_ID);
    inOrder.verify(scopeManagerMock).getAllScopes();
    inOrder.verify(scopeManagerMock).getIndexFor(scopeMock, baseType);
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

    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;
    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations1 = Lists.newArrayList();
    filteredVariations1.add(mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations2 = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class));

    // when
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);
    when(storageManagerMock.getAllVariations(baseType, DEFAULT_ID)).thenReturn(variations);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock1, scopeMock2));
    when(scopeManagerMock.getIndexFor(scopeMock1, baseType)).thenReturn(indexMock1);
    when(scopeManagerMock.getIndexFor(scopeMock2, baseType)).thenReturn(indexMock2);
    when(scopeMock1.filter(variations)).thenReturn(filteredVariations1);
    when(scopeMock2.filter(variations)).thenReturn(filteredVariations2);

    // action
    instance.addEntity(type, DEFAULT_ID);

    // verify
    verify(typeRegistryMock).getBaseClass(type);
    verify(storageManagerMock).getAllVariations(baseType, DEFAULT_ID);
    verify(scopeManagerMock).getAllScopes();
    verify(scopeManagerMock).getIndexFor(scopeMock1, baseType);
    verify(scopeManagerMock).getIndexFor(scopeMock2, baseType);
    verify(scopeMock1).filter(variations);
    verify(scopeMock2).filter(variations);
    verify(indexMock1).add(filteredVariations1);
    verify(indexMock2).add(filteredVariations2);
  }

  @Test(expected = IndexException.class)
  public void testAddEntityStorageManagerThrowsAnIOException() throws IOException, IndexException {
    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;
    // when
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);
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

    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;
    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations = Lists.newArrayList();
    filteredVariations.add(mock(SubModel.class));

    // when
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);
    when(storageManagerMock.getAllVariations(baseType, DEFAULT_ID)).thenReturn(variations);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, baseType)).thenReturn(indexMock);
    when(scopeMock.filter(variations)).thenReturn(filteredVariations);
    doThrow(IndexException.class).when(indexMock).add(filteredVariations);

    try {
      // action
      instance.addEntity(type, DEFAULT_ID);
    } finally {
      // verify
      InOrder inOrder = Mockito.inOrder(typeRegistryMock, storageManagerMock, scopeManagerMock, scopeMock, indexMock);
      inOrder.verify(typeRegistryMock).getBaseClass(type);
      inOrder.verify(storageManagerMock).getAllVariations(baseType, DEFAULT_ID);
      inOrder.verify(scopeManagerMock).getAllScopes();
      inOrder.verify(scopeManagerMock).getIndexFor(scopeMock, baseType);
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
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);
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

    Class<? extends DomainEntity> type = SubModel.class;
    Class<? extends DomainEntity> baseType = ExplicitlyAnnotatedModel.class;

    // when
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, baseType)).thenReturn(indexMock);

    // action
    instance.deleteEntity(type, DEFAULT_ID);

    //verify
    verify(typeRegistryMock).getBaseClass(type);
    verify(scopeManagerMock).getAllScopes();
    verify(scopeManagerMock).getIndexFor(scopeMock, baseType);
    verify(indexMock).deleteById(DEFAULT_ID);
  }

  @Test
  public void testDeleteMultipleScopes() throws IndexException {
    // setup
    Scope scopeMock1 = mock(Scope.class);
    Scope scopeMock2 = mock(Scope.class);
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    Class<? extends DomainEntity> type = SubModel.class;
    Class<? extends DomainEntity> baseType = ExplicitlyAnnotatedModel.class;

    // when
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock1, scopeMock2));
    when(scopeManagerMock.getIndexFor(scopeMock1, baseType)).thenReturn(indexMock1);
    when(scopeManagerMock.getIndexFor(scopeMock2, baseType)).thenReturn(indexMock2);

    // action
    instance.deleteEntity(type, DEFAULT_ID);

    //verify
    verify(typeRegistryMock).getBaseClass(type);
    verify(scopeManagerMock).getAllScopes();
    verify(scopeManagerMock).getIndexFor(scopeMock1, baseType);
    verify(scopeManagerMock).getIndexFor(scopeMock2, baseType);
    verify(indexMock1).deleteById(DEFAULT_ID);
    verify(indexMock2).deleteById(DEFAULT_ID);
  }

  @Test(expected = IndexException.class)
  public void testDeleteMultipleScopesFirstThrowsAnException() throws IndexException {
    // setup
    Scope scopeMock1 = mock(Scope.class);
    Scope scopeMock2 = mock(Scope.class);
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    Class<? extends DomainEntity> type = SubModel.class;
    Class<? extends DomainEntity> baseType = ExplicitlyAnnotatedModel.class;

    // when
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock1, scopeMock2));
    when(scopeManagerMock.getIndexFor(scopeMock1, baseType)).thenReturn(indexMock1);
    when(scopeManagerMock.getIndexFor(scopeMock2, baseType)).thenReturn(indexMock2);
    doThrow(IndexException.class).when(indexMock1).deleteById(DEFAULT_ID);

    try {
      // action
      instance.deleteEntity(type, DEFAULT_ID);
    } finally {
      //verify
      verify(typeRegistryMock).getBaseClass(type);
      verify(scopeManagerMock).getAllScopes();
      verify(scopeManagerMock).getIndexFor(scopeMock1, baseType);
      verify(indexMock1).deleteById(DEFAULT_ID);
      verify(scopeManagerMock, times(0)).getIndexFor(scopeMock2, baseType);
      verifyZeroInteractions(indexMock2);
    }
  }
}
