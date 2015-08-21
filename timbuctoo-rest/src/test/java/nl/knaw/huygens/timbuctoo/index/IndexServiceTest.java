package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexServiceTest {

  public static final String REQUEST_ID = "requestId";
  public static final ActionType ACTION_TYPE = ActionType.MOD;
  public static final Action ACTION_WITH_REQUEST_ID = Action.forRequestWithId(ACTION_TYPE, REQUEST_ID);
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ENTITY_ID = "entityId";
  public static final Action ACTION_FOR_SINGLE_ENTITY = new Action(ACTION_TYPE, TYPE, ENTITY_ID);
  private Indexer indexer;
  private IndexerFactory indexerFactory;
  private IndexRequest indexRequestFromIndexRequests;
  private IndexRequests indexRequests;
  private IndexService instance;
  private IndexRequestFactory indexRequestFactory;
  private IndexRequestImpl indexRequestFromFactory;


  @Before
  public void setUp() throws Exception {
    setupIndexerFactory();
    setupIndexRequests();
    setupIndexRequestFactory();
    instance = new IndexService(mock(Broker.class), indexRequests, indexRequestFactory, indexerFactory);
  }

  private void setupIndexRequestFactory() {
    indexRequestFactory = mock(IndexRequestFactory.class);
    indexRequestFromFactory = mock(IndexRequestImpl.class);
    when(indexRequestFactory.forEntity(TYPE, ENTITY_ID)).thenReturn(indexRequestFromFactory);
  }

  private void setupIndexerFactory() {
    indexer = mock(Indexer.class);
    indexerFactory = mock(IndexerFactory.class);
    when(indexerFactory.create(ACTION_TYPE)).thenReturn(indexer);
  }

  private void setupIndexRequests() {
    indexRequestFromIndexRequests = mock(IndexRequestImpl.class);
    indexRequests = mock(IndexRequests.class);
    when(indexRequests.get(REQUEST_ID)).thenReturn(indexRequestFromIndexRequests);
  }

  @Test
  public void executeActionForActionWithRequestIdSetsTheIndexRequestToInProgressAndLetAnIndexerExcecuteTheAction() throws Exception {
    // action
    instance.executeAction(ACTION_WITH_REQUEST_ID);

    // verify
    indexRequestFromIndexRequests.execute(indexer);
  }

  @Test
  public void executeActionForActionForSingleItemLetsTheIndexerExecuteACreateIndexRequestForTheAction() throws Exception {
    // action
    instance.executeAction(ACTION_FOR_SINGLE_ENTITY);

    // verify
    indexRequestFromFactory.execute(indexer);
  }
}
