package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexServiceTest {

  public static final String REQUEST_ID = "requestId";
  public static final ActionType ACTION_TYPE = ActionType.MOD;
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ENTITY_ID = "entityId";
  public static final Action ACTION = new Action(ACTION_TYPE, TYPE, ENTITY_ID);
  private Indexer indexer;
  private IndexerFactory indexerFactory;
  private IndexService instance;
  private IndexRequestFactory indexRequestFactory;
  private IndexRequest indexRequest;


  @Before
  public void setUp() throws Exception {
    setupIndexerFactory();
    setupIndexRequestFactory();
    instance = new IndexService(mock(Broker.class), indexRequestFactory, indexerFactory);
  }

  private void setupIndexRequestFactory() {
    indexRequestFactory = mock(IndexRequestFactory.class);
    indexRequest = mock(IndexRequest.class);
    when(indexRequestFactory.forAction(any(Action.class))).thenReturn(indexRequest);
  }

  private void setupIndexerFactory() {
    indexer = mock(Indexer.class);
    indexerFactory = mock(IndexerFactory.class);
    when(indexerFactory.create(any(IndexRequest.class))).thenReturn(indexer);
  }


  @Test
  public void executeActionForActionForSingleItemLetsTheIndexerExecuteACreateIndexRequestForTheAction() throws Exception {
    // action
    instance.executeAction(ACTION);

    // verify
    InOrder inOrder = inOrder(indexRequestFactory, indexRequest);
    inOrder.verify(indexRequestFactory).forAction(ACTION);
    inOrder.verify(indexRequest).execute(indexer);
  }
}
