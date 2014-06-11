package nl.knaw.huygens.timbuctoo.vre;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexNameCreator;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class VREManagerTest {

  @Mock
  private Map<String, Index> indexMapMock;

  private IndexNameCreator indexNameCreatorMock;

  private VREManager instance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    indexNameCreatorMock = mock(IndexNameCreator.class);
    instance = new VREManager(indexMapMock, indexNameCreatorMock);
  }

  @Test
  public void testGetIndexFor() {
    // mock
    VRE vreMock = mock(VRE.class);

    Class<ExplicitlyAnnotatedModel> type = ExplicitlyAnnotatedModel.class;
    String indexName = "indexName";

    // when
    when(indexNameCreatorMock.getIndexNameFor(vreMock, type)).thenReturn(indexName);

    // action
    instance.getIndexFor(vreMock, type);

    // verify
    InOrder inOrder = Mockito.inOrder(indexNameCreatorMock, indexMapMock);
    inOrder.verify(indexNameCreatorMock).getIndexNameFor(vreMock, type);
    inOrder.verify(indexMapMock).get(indexName);
  }

  @Test
  public void testGetIndexForWhenIndexDoesNotExist() {
    // mock
    VRE vreMock = mock(VRE.class);

    Class<ExplicitlyAnnotatedModel> type = ExplicitlyAnnotatedModel.class;
    String indexName = "indexName";

    // when
    when(indexNameCreatorMock.getIndexNameFor(vreMock, type)).thenReturn(indexName);

    // action
    Index index = instance.getIndexFor(vreMock, type);

    // verify
    InOrder inOrder = Mockito.inOrder(indexNameCreatorMock, indexMapMock);
    inOrder.verify(indexNameCreatorMock).getIndexNameFor(vreMock, type);
    inOrder.verify(indexMapMock).get(indexName);

    assertThat(index, is(instanceOf(VREManager.NoOpIndex.class)));
  }
}
