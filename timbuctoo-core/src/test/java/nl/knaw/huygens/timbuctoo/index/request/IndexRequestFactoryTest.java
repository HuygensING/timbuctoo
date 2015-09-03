package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import test.variation.model.projecta.ProjectADomainEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class IndexRequestFactoryTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID = "id";
  private IndexRequestFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new IndexRequestFactory(mock(Repository.class));
  }

  @Test
  public void forCollectionOfCreatesACollectionIndexRequest() throws Exception {
    // action
    IndexRequest indexRequest = instance.forCollectionOf(TYPE);

    // verify
    assertThat(indexRequest, Matchers.is(Matchers.instanceOf(CollectionIndexRequest.class)));
  }

  @Test
  public void forEntityCreatesAEntityIndexRequest() throws Exception {
    // action
    IndexRequest indexRequest = instance.forEntity(TYPE, ID);

    // verify
    assertThat(indexRequest, Matchers.is(Matchers.instanceOf(EntityIndexRequest.class)));
  }
}
