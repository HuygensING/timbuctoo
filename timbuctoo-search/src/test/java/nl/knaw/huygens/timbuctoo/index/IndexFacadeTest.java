package nl.knaw.huygens.timbuctoo.index;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

public class IndexFacadeTest {

  private ScopeManager scopeManagerMock;
  private TypeRegistry typeRegistryMock;
  private IndexFacade instance;

  @Before
  public void setUp() {
    scopeManagerMock = mock(ScopeManager.class);
    typeRegistryMock = mock(TypeRegistry.class);
    instance = new IndexFacade(scopeManagerMock, typeRegistryMock);
  }

  @Test
  public void testAddEntityInOneIndex() throws IndexException {
    // mock
    Scope scopeMock = mock(Scope.class);
    Index indexMock = mock(Index.class);

    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;

    // when
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock));
    when(scopeManagerMock.getIndexFor(scopeMock, baseType)).thenReturn(indexMock);
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);

    // action
    instance.addEntity(type, "id01234");

    // verify
    InOrder inOrder = Mockito.inOrder(scopeManagerMock, typeRegistryMock, indexMock);
    inOrder.verify(typeRegistryMock).getBaseClass(type);
    inOrder.verify(scopeManagerMock).getAllScopes();
    inOrder.verify(scopeManagerMock).getIndexFor(scopeMock, baseType);
    inOrder.verify(indexMock).add(baseType, "id01234");
  }

  @Test
  public void testAddEntityInMultipleIndexes() throws IndexException {
    // mock
    Scope scopeMock1 = mock(Scope.class);
    Scope scopeMock2 = mock(Scope.class);
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;

    // when
    when(scopeManagerMock.getAllScopes()).thenReturn(Lists.newArrayList(scopeMock1, scopeMock2));
    when(scopeManagerMock.getIndexFor(scopeMock1, baseType)).thenReturn(indexMock1);
    when(scopeManagerMock.getIndexFor(scopeMock2, baseType)).thenReturn(indexMock2);
    doReturn(baseType).when(typeRegistryMock).getBaseClass(type);

    // action
    instance.addEntity(type, "id01234");

    // verify
    verify(typeRegistryMock).getBaseClass(type);
    verify(scopeManagerMock).getAllScopes();
    verify(scopeManagerMock).getIndexFor(scopeMock1, baseType);
    verify(scopeManagerMock).getIndexFor(scopeMock2, baseType);
    verify(indexMock1).add(baseType, "id01234");
    verify(indexMock2).add(baseType, "id01234");
  }
}
