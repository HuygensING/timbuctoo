package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class IndexRequestFactoryTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID = "id";
  public static final ActionType ACTION_TYPE = ActionType.MOD;
  private IndexRequestFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new IndexRequestFactory(mock(IndexerFactory.class), mock(Repository.class));
  }

  @Test
  public void forCollectionOfCreatesACollectionIndexRequest() throws Exception {
    // action
    IndexRequest indexRequest = instance.forCollectionOf(ActionType.MOD, TYPE);

    // verify
    assertThat(indexRequest, is(instanceOf(CollectionIndexRequest.class)));
  }

  @Test
  public void forEntityCreatesAnEntityIndexRequest() throws Exception {
    // action
    IndexRequest indexRequest = instance.forEntity(ACTION_TYPE, TYPE, ID);

    // verify
    assertThat(indexRequest, is(instanceOf(EntityIndexRequest.class)));
  }

  @Test
  public void forActionCreatesACollectionIndexRequestIfTheActionIsForMultipleEntities() {
    // action
    IndexRequest indexRequest = instance.forAction(Action.multiUpdateActionFor(TYPE));

    // verify
    assertThat(indexRequest, is(instanceOf(CollectionIndexRequest.class)));
  }

  @Test
  public void forActionCreatesAnEnityIndexRequestIfTheActionIsForASingleEntity() {
    // action
    IndexRequest indexRequest = instance.forAction(new Action(ActionType.MOD, TYPE, ID));

    // verify
    assertThat(indexRequest, is(instanceOf(EntityIndexRequest.class)));
  }
}
