package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;

public class IndexerFactoryTest {

  private IndexerFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new IndexerFactory(mock(Repository.class), mock(IndexManager.class));
  }

  @Test
  public void createCreatesAnAddIndexerIfTheActionTypeIsADD() {
    // action
    Indexer indexer = instance.create(ActionType.ADD);

    // verify
    assertThat(indexer, is(instanceOf(AddIndexer.class)));
  }

  @Test
  public void createCreatesAnUpdateIndexerIfTheActionTypeIsMOD() {
    // action
    Indexer indexer = instance.create(ActionType.MOD);

    // verify
    assertThat(indexer, is(instanceOf(UpdateIndexer.class)));
  }

  @Test
  public void createCreatesADeleteIndexerIfTheActionTypeIsDEL(){
    // action
    Indexer indexer = instance.create(ActionType.DEL);

    // verify
    assertThat(indexer, is(Matchers.instanceOf(DeleteIndexer.class)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createThrowsAnIllegalArgumentExceptionIfTheActionTypeIsEnd(){
    // action
    instance.create(ActionType.END);
  }
}
