package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;
import nl.knaw.huygens.timbuctoo.persistence.request.PersistenceRequestFactory;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;
import test.rest.model.projecta.ProjectAPerson;
import test.rest.model.projecta.ProjectARelation;

import javax.jms.JMSException;

import static nl.knaw.huygens.timbuctoo.messages.ActionType.ADD;
import static nl.knaw.huygens.timbuctoo.messages.ActionType.DEL;
import static nl.knaw.huygens.timbuctoo.messages.ActionType.END;
import static nl.knaw.huygens.timbuctoo.messages.ActionType.MOD;
import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


public class ChangeHelperTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID = "id";
  public static final ProjectADomainEntity DOMAIN_ENTITY = new ProjectADomainEntity();
  public static final Class<ProjectAPerson> SOURCE_TYPE = ProjectAPerson.class;
  public static final String SOURCE_ID = "sourceId";
  public static final String TARGET_ID = "targetId";
  public static final Class<ProjectADomainEntity> TARGET_TYPE = ProjectADomainEntity.class;
  public static final Class<ProjectARelation> RELATION_TYPE = ProjectARelation.class;
  private Producer persistenceProducer;
  private ChangeHelper instance;
  private Broker broker;
  private Producer indexProducer;
  private TypeRegistry typeRegistry;

  @Before
  public void setUp() throws Exception {
    setupBroker();
    setupTypeRegistry();
    instance = new ChangeHelper(broker, typeRegistry, new PersistenceRequestFactory(mock(Repository.class), mock(PersisterFactory.class)), new IndexRequestFactory(mock(IndexerFactory.class), mock(Repository.class), mock(TypeRegistry.class)));
  }

  private void setupTypeRegistry() throws ModelException {
    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init(SOURCE_TYPE.getPackage().getName());
  }

  private void setupBroker() throws JMSException {
    persistenceProducer = mock(Producer.class);
    broker = mock(Broker.class);
    when(broker.getProducer(ChangeHelper.PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE)).thenReturn(persistenceProducer);
    indexProducer = mock(Producer.class);
    when(broker.getProducer(ChangeHelper.INDEX_MSG_PRODUCER, Broker.INDEX_QUEUE)).thenReturn(indexProducer);
  }

  @Test
  public void sendPersistMessageSendsAnActionCreatedFromTheParametersOfTheMethod() throws JMSException {
    // setup
    ActionType actionType = ADD;

    // action
    PersistenceRequest persistenceRequest = mock(PersistenceRequest.class);
    Action action = new Action();
    when(persistenceRequest.toAction()).thenReturn(action);
    instance.sendPersistMessage(persistenceRequest);

    // verify
    verify(persistenceProducer).send(action);
  }

  @Test
  public void notifyChangeWithActionTypeADDSendsAnAddActionToTheIndexAndThePersistenceQueue() throws JMSException {
    // action
    instance.notifyChange(ADD, TYPE, DOMAIN_ENTITY, ID);

    // verify
    verifyActionIsSendToIndexAndPersistenceQueue(ADD, TYPE);

  }

  private void verifyActionIsSendToIndexAndPersistenceQueue(ActionType actionType, Class<? extends DomainEntity> type) throws JMSException {
    // verify
    verify(indexProducer).send(argThat(likeAction() //
      .withActionType(actionType) //
      .withType(type) //
      .withId(ID)));

    verify(persistenceProducer).send(argThat(likeAction() //
      .withActionType(actionType) //
      .withType(type) //
      .withId(ID)));
  }

  @Test
  public void notifyChangeWithActionTypeMODSendsAModificationActionToTheIndexAndAnAddActionThePersistenceQueue() throws JMSException {
    // action
    instance.notifyChange(MOD, TYPE, DOMAIN_ENTITY, ID);

    // verify
    // verify
    verify(indexProducer).send(argThat(likeAction() //
      .withActionType(MOD) //
      .withType(TYPE) //
      .withId(ID)));

    verify(persistenceProducer).send(argThat(likeAction() //
      .withActionType(ADD) //
      .withType(TYPE) //
      .withId(ID)));

  }

  @Test
  public void notifyChangeWithActionTypeDELSendsADeleteActionToTheIndexQueue() throws JMSException {
    // action
    instance.notifyChange(DEL, TYPE, DOMAIN_ENTITY, ID);

    // verify
    verify(indexProducer).send(argThat(likeAction() //
      .withActionType(DEL) //
      .withType(TYPE) //
      .withId(ID)));

  }

  @Test
  public void notifyChangeWithActionTypeENDDoesNotSendsActions() {
    // action
    instance.notifyChange(END, TYPE, DOMAIN_ENTITY, ID);

    // verify
    verifyZeroInteractions(indexProducer, persistenceProducer);
  }

  @Test
  public void notifyChangeForRelationWithActionTypeADDSendsAnIndexUpdateForItsSourceAndTarget() throws JMSException {
    // action
    instance.notifyChange(ADD, RELATION_TYPE, relation(), ID);

    // verify
    verifyIndexUpdateForSource();

    verifyIndexUpdateForTarget();

  }

  private void verifyIndexUpdateForTarget() throws JMSException {
    verify(indexProducer).send(argThat(likeAction() //
      .withActionType(MOD) //
      .withType(TARGET_TYPE) //
      .withId(TARGET_ID)));
  }

  private void verifyIndexUpdateForSource() throws JMSException {
    verify(indexProducer).send(argThat(likeAction() //
      .withActionType(MOD) //
      .withType(SOURCE_TYPE) //
      .withId(SOURCE_ID)));
  }

  private DomainEntity relation() {
    return new ProjectARelation(ID, TypeNames.getInternalName(SOURCE_TYPE), SOURCE_ID, TypeNames.getInternalName(TARGET_TYPE), TARGET_ID);
  }


  @Test
  public void notifyChangeForRelationWithActionTypeMODSendsAnIndexUpdateForItsSourceAndTarget() throws JMSException {
    // action
    instance.notifyChange(MOD, RELATION_TYPE, relation(), ID);

    // verify
    verifyIndexUpdateForSource();

    verifyIndexUpdateForTarget();

  }

  @Test
  public void notifyChangeForRelationWithActionTypeDELSendsAnIndexUpdateForItsSourceAndTarget() throws JMSException {
    // action
    instance.notifyChange(DEL, RELATION_TYPE, relation(), ID);

    // verify
    verifyIndexUpdateForSource();

    verifyIndexUpdateForTarget();
  }

  @Test
  public void notifyChangeForRelationWithActionTypeENDDoesNotSendActions() {
    // action
    instance.notifyChange(END, RELATION_TYPE, relation(), ID);

    // verify
    verifyZeroInteractions(indexProducer, persistenceProducer);
  }

}
