package nl.knaw.huygens.timbuctoo.index.indexer;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractIndexerTest {
  protected static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  protected static final String ID_1 = "id1";
  protected static final String ID_2 = "id2";
  protected Repository repository;
  protected IndexManager indexManager;
  protected Indexer instance;
  protected List<ProjectADomainEntity> entities;

  @Before
  public void setUp() throws Exception {
    setupRepository();
    indexManager = mock(IndexManager.class);
    instance = createInstance();
  }

  protected abstract Indexer createInstance();

  private void setupRepository() {
    repository = mock(Repository.class);
    entities = Lists.newArrayList();
    entities.add(createEntityWithID(ID_1));
    entities.add(createEntityWithID(ID_2));
    when(repository.getDomainEntities(TYPE)).thenReturn(StorageIteratorStub.newInstance(entities));
  }

  private ProjectADomainEntity createEntityWithID(String id) {
    ProjectADomainEntity entity = new ProjectADomainEntity();
    entity.setId(id);
    return entity;
  }

  @Test
  public void executeForSetsTheIndexRequestStatusToIN_PROGRESSWhenStartingAndToDONEWhenFinished() throws Exception {
    // setup
    IndexRequest request = mock(IndexRequest.class);
    doReturn(TYPE).when(request).getType();

    // action
    instance.executeFor(request);

    // verify
    InOrder inOrder = inOrder(request, repository, indexManager);
    inOrder.verify(request).inProgress();
    inOrder.verify(repository).getDomainEntities(TYPE);
    verifyIndexActionExecuted(inOrder, TYPE, ID_1);
    verifyIndexActionExecuted(inOrder, TYPE, ID_2);
    inOrder.verify(request).done();

  }

  @Test
  public void executeForCallsIndexManagerForEachItemFoundByTheRepository() throws Exception {
    // action
    instance.executeFor(IndexRequest.forType(TYPE));

    // verify
    verifyIndexActionExecuted(TYPE, ID_1);
    verifyIndexActionExecuted(TYPE, ID_2);
  }

  @Test(expected = IndexException.class)
  public void executeForThrowsAnIndexExceptionWhenTheIndexManagerDoes() throws Exception {
    // setup
    throwAnIndexExceptionWhenIndexMethodExecuted(TYPE, ID_1);

    // action
    instance.executeFor(IndexRequest.forType(TYPE));
  }

  protected abstract void verifyIndexActionExecuted(Class<? extends DomainEntity> type, String id) throws IndexException;

  protected abstract void verifyIndexActionExecuted(InOrder inOrder, Class<? extends DomainEntity> type, String id) throws IndexException;

  protected abstract void throwAnIndexExceptionWhenIndexMethodExecuted(Class<? extends DomainEntity> type, String id) throws IndexException;
}
