package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import org.junit.Test;
import org.mockito.Mockito;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class IndexServiceTest {

  public static final String REQUEST_ID = "requestId";
  public static final ActionType ACTION_TYPE = ActionType.MOD;
  public static final Action ACTION_WITH_REQUEST_ID = Action.forRequestWithId(ACTION_TYPE, REQUEST_ID);
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;



  @Test
  public void executeActionForActionWithRequestIdSetsTheIndexRequestToInProgressAndLetAnIndexerExcecuteTheAction() throws Exception {
    // setup
    IndexRequests indexRequests = mock(IndexRequests.class);
    IndexRequest indexRequest = IndexRequest.forType(TYPE);
    Mockito.when(indexRequests.get(REQUEST_ID)).thenReturn(indexRequest);
    IndexerFactory indexerFactory = mock(IndexerFactory.class);
    Indexer indexer = mock(Indexer.class);
    Mockito.when(indexerFactory.create(ACTION_TYPE)).thenReturn(indexer);
    IndexManager indexManager = mock(IndexManager.class);
    IndexService instance = new IndexService(indexManager, mock(Broker.class), indexRequests, indexerFactory);

    // action
    instance.executeAction(ACTION_WITH_REQUEST_ID);


    // verify
    assertThat(indexRequest.getStatus(), is(IndexRequest.Status.REQUESTED));

    verify(indexer).executeFor(indexRequest);
    verifyZeroInteractions(indexManager);
  }
}
