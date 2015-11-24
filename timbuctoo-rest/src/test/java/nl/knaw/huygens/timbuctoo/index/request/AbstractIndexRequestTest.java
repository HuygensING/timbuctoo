package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractIndexRequestTest {
  public static final ActionType ACTION_TYPE = ActionType.ADD;
  protected static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  private Indexer indexer;
  private IndexRequest instance;
  private IndexerFactory indexerFactory;

  @Before
  public void setup() {
    setupIndexerFactory();
    instance = createInstance();
  }

  private void setupIndexerFactory() {
    indexer = mock(Indexer.class);
    indexerFactory = mock(IndexerFactory.class);
    when(indexerFactory.create(ACTION_TYPE)).thenReturn(indexer);
  }

  protected IndexRequest getInstance() {
    return instance;
  }

  protected abstract IndexRequest createInstance();

  protected IndexerFactory getIndexerFactory() {

    return indexerFactory;
  }

  protected Indexer getIndexer() {
    return indexer;
  }

  @Test
  abstract void toActionCreatesAnActionThatCanBeUsedByTheProducer();
}
