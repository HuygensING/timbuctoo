package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.Indexer;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.mock;

public abstract class AbstractIndexRequestTest {
  protected static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  protected Indexer indexer;
  private IndexRequest instance;

  @Before
  public void setup() {
    instance = createInstance();
    indexer = mock(Indexer.class);
  }

  protected IndexRequest getInstance() {
    return instance;
  }

  protected abstract IndexRequest createInstance();

  @Test
  abstract void toActionCreatesAnActionThatCanBeUsedByTheProducer();
}
