package nl.knaw.huygens.timbuctoo.index.indexer;

import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexerFactoryTest {

  private IndexerFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new IndexerFactory(mock(IndexManager.class));
  }

  @Test
  public void createCreatesAnAddIndexerIfTheActionTypeIsADD() {
    // action
    Indexer indexer = instance.create(createIndexRequestWithActionType(ActionType.ADD));

    // verify
    assertThat(indexer, is(instanceOf(AddIndexer.class)));
  }

  private IndexRequest createIndexRequestWithActionType(ActionType actionType) {
    IndexRequest indexRequest = mock(IndexRequest.class);
    when(indexRequest.getActionType()).thenReturn(actionType);
    return indexRequest;
  }

  @Test
  public void createCreatesAnUpdateIndexerIfTheActionTypeIsMOD() {
    // action
    Indexer indexer = instance.create(createIndexRequestWithActionType(ActionType.MOD));

    // verify
    assertThat(indexer, is(instanceOf(UpdateIndexer.class)));
  }

  @Test
  public void createCreatesADeleteIndexerIfTheActionTypeIsDEL(){
    // action
    Indexer indexer = instance.create(createIndexRequestWithActionType(ActionType.DEL));

    // verify
    assertThat(indexer, is(Matchers.instanceOf(DeleteIndexer.class)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createThrowsAnIllegalArgumentExceptionIfTheActionTypeIsEnd(){
    // action
    instance.create(createIndexRequestWithActionType(ActionType.END));
  }
}
