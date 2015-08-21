package nl.knaw.huygens.timbuctoo.index;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import test.variation.model.projecta.ProjectADomainEntity;

import static org.hamcrest.MatcherAssert.assertThat;

public class IndexRequestFactoryTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID = "id";
  private IndexRequestFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new IndexRequestFactory();
  }

  @Test
  public void forCollectionCreatesACollectionIndexRequest() throws Exception {
    // action
    IndexRequest indexRequest = instance.forType(TYPE);

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
