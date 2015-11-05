package nl.knaw.huygens.timbuctoo.index.indexer;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AddIndexerTest  {

  private static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  private static final String ID = "id";
  private AddIndexer instance;
  private IndexManager indexManager;

  @Before
  public void setup() {
    indexManager = mock(IndexManager.class);
    instance = new AddIndexer(indexManager);
  }

  @Test
  public void executeIndexActionClassIndexManagersAddEntity() throws Exception {
    // action
    instance.executeIndexAction(TYPE, ID);

    // verify
    verify(indexManager).addEntity(TYPE, ID);
  }

  @Test(expected = IndexException.class)
  public void executeIndexActionThrowsAnIndexExctionWhenTheIndexManagerDoes() throws Exception {
    // setup
    doThrow(IndexException.class).when(indexManager).addEntity(TYPE, ID);

    // action
    instance.executeIndexAction(TYPE, ID);
  }
}
