package nl.knaw.huygens.timbuctoo.index;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddIndexerTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID_1 = "id1";
  public static final String ID_2 = "id2";
  private Repository repository;
  private IndexManager indexManager;
  private AddIndexer instance;
  private List<ProjectADomainEntity> entities;

  @Before
  public void setUp() throws Exception {
    setupRepository();
    indexManager = mock(IndexManager.class);
    instance = new AddIndexer(repository, indexManager);
  }

  private void setupRepository() {
    repository = mock(Repository.class);
    entities = Lists.newArrayList();
    entities.add(createEntityWithID(ID_1));
    entities.add(createEntityWithID(ID_2));
    when(repository.getDomainEntities(TYPE)).thenReturn(StorageIteratorStub.newInstance(entities));
  }

  @Test
  public void executeForCallsIndexManagersAddEntityForEachItemFoundInTheDatabase() throws Exception {
    // action
    instance.executeFor(IndexRequest.forType(TYPE));

    // verify
    verify(indexManager).addEntity(TYPE, ID_1);
    verify(indexManager).addEntity(TYPE, ID_2);
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
    inOrder.verify(indexManager).addEntity(TYPE, ID_1);
    inOrder.verify(indexManager).addEntity(TYPE, ID_2);
    inOrder.verify(request).done();

  }

  @Test(expected = IndexException.class)
  public void executeForThrowsAnIndexExceptionWhenTheIndexManagerDoes() throws Exception {
    // setup
    doThrow(new IndexException()).when(indexManager).addEntity(TYPE, ID_1);

    // action
    instance.executeFor(IndexRequest.forType(TYPE));
  }

  private ProjectADomainEntity createEntityWithID(String id) {
    ProjectADomainEntity entity = new ProjectADomainEntity();
    entity.setId(id);
    return entity;
  }
}
