package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.index.ScopeManager.NoOpIndex;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class ScopeManagerTest {

  private List<Scope> scopes;

  @Mock
  private Map<String, Index> indexMapMock;

  private IndexNameCreator indexNameCreatorMock;

  private ScopeManager instance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    scopes = Lists.newArrayList(mock(Scope.class), mock(Scope.class));
    indexNameCreatorMock = mock(IndexNameCreator.class);
    instance = new ScopeManager(scopes, indexMapMock, indexNameCreatorMock);
  }

  @Test
  public void testGetAllScopes() {

    assertThat(instance.getAllScopes(), equalTo(scopes));
  }

  @Test
  public void testGetIndexFor() {
    // mock
    Scope scopeMock = mock(Scope.class);

    Class<ExplicitlyAnnotatedModel> type = ExplicitlyAnnotatedModel.class;
    String indexName = "indexName";

    // when
    when(indexNameCreatorMock.getIndexNameFor(scopeMock, type)).thenReturn(indexName);

    // action
    instance.getIndexFor(scopeMock, type);

    // verify
    InOrder inOrder = Mockito.inOrder(indexNameCreatorMock, indexMapMock);
    inOrder.verify(indexNameCreatorMock).getIndexNameFor(scopeMock, type);
    inOrder.verify(indexMapMock).get(indexName);
  }

  @Test
  public void testGetIndexForWhenIndexDoesNotExist() {
    // mock
    Scope scopeMock = mock(Scope.class);

    Class<ExplicitlyAnnotatedModel> type = ExplicitlyAnnotatedModel.class;
    String indexName = "indexName";

    // when
    when(indexNameCreatorMock.getIndexNameFor(scopeMock, type)).thenReturn(indexName);

    // action
    Index index = instance.getIndexFor(scopeMock, type);

    // verify
    InOrder inOrder = Mockito.inOrder(indexNameCreatorMock, indexMapMock);
    inOrder.verify(indexNameCreatorMock).getIndexNameFor(scopeMock, type);
    inOrder.verify(indexMapMock).get(indexName);

    assertThat(index, is(instanceOf(NoOpIndex.class)));
  }
}
