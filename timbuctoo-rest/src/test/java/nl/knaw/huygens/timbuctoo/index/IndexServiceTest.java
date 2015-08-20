package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IndexServiceTest {

  public static final String REQUEST_ID = "requestId";
  public static final ActionType ACTION_TYPE = ActionType.MOD;
  public static final Action ACTION_WITH_REQUEST_ID = Action.forRequestWithId(ACTION_TYPE, REQUEST_ID);
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ENTITY_ID = "entityId";
  public static final Action ACTION_FOR_SINGLE_ENTITY = new Action(ACTION_TYPE, TYPE, ENTITY_ID);
  private Indexer indexer;
  private IndexerFactory indexerFactory;
  private IndexRequest indexRequest;
  private IndexRequests indexRequests;
  private IndexService instance;


  @Before
  public void setUp() throws Exception {
    setupIndexerFactory();
    setupIndexRequests();
    instance = new IndexService(mock(Broker.class), indexRequests, new IndexRequestFactory(), indexerFactory);
  }

  private void setupIndexerFactory() {
    indexer = mock(Indexer.class);
    indexerFactory = mock(IndexerFactory.class);
    Mockito.when(indexerFactory.create(ACTION_TYPE)).thenReturn(indexer);
  }

  private void setupIndexRequests() {
    indexRequest = IndexRequest.forType(TYPE);
    indexRequests = mock(IndexRequests.class);
    Mockito.when(indexRequests.get(REQUEST_ID)).thenReturn(indexRequest);
  }

  @Test
  public void executeActionForActionWithRequestIdSetsTheIndexRequestToInProgressAndLetAnIndexerExcecuteTheAction() throws Exception {
    // action
    instance.executeAction(ACTION_WITH_REQUEST_ID);

    // verify
    verify(indexer).executeFor(indexRequest);
  }

  @Test
  public void executeActionForActionForSingleItemLetsTheIndexerExecuteACreateIndexRequestForTheAction() throws Exception {
    // action
    instance.executeAction(ACTION_FOR_SINGLE_ENTITY);

    // verify
    verify(indexer).executeFor(argThat(IndexRequestMatcher.likeIndexRequest().forType(TYPE).forId(ENTITY_ID)));
  }
}
