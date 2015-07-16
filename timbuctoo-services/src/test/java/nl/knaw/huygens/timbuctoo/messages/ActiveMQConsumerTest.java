package nl.knaw.huygens.timbuctoo.messages;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import static nl.knaw.huygens.timbuctoo.messages.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActiveMQConsumerTest {
  private static final String QUEUE = "queue";
  private static final String NAME = "name";
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String PACKAGE_NAME = TYPE.getPackage().getName();
  public static final ActionType ACTION_TYPE = ActionType.ADD;
  public static final String ID = "id";
  private Session session;
  private ActiveMQConsumer instance;
  private MessageConsumer consumer;

  @Before
  public void setup() throws JMSException, ModelException {
    ConnectionFactory connectionFactory = setupConnectionFactory();
    TypeRegistry typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init(PACKAGE_NAME);
    this.instance = new ActiveMQConsumer(connectionFactory, QUEUE, NAME, typeRegistry);
  }

  private ConnectionFactory setupConnectionFactory() throws JMSException {
    session = mock(Session.class);
    consumer = mock(MessageConsumer.class);
    when(session.createConsumer(any(Destination.class))).thenReturn(consumer);

    Connection connection = mock(Connection.class);
    when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

    ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    when(connectionFactory.createConnection()).thenReturn(connection);

    return connectionFactory;
  }


  @Test
  public void receiveCreatesAnActionFromAMessage() throws JMSException {
    // setup
    Message message = mock(Message.class);
    when(message.getStringProperty(Broker.PROP_ACTION)).thenReturn(ACTION_TYPE.getStringRepresentation());
    when(message.getStringProperty(Broker.PROP_DOC_TYPE)).thenReturn(TypeNames.getInternalName(TYPE));
    when(message.getStringProperty(Broker.PROP_DOC_ID)).thenReturn(ID);
    when(message.getBooleanProperty(Broker.PROP_IS_MULTI_ENTITY)).thenReturn(false);
    when(consumer.receive(anyInt())).thenReturn(message);

    // action
    Action action = instance.receive();

    // verify
    assertThat(action, is(likeAction()//
        .withType(TYPE) //
        .withActionType(ACTION_TYPE) //
        .withId(ID) //
        .withForMultiEntitiesFlag(false)));
  }

  @Test
  public void receiveCreatesAnActionWithoutIdWhenItIsForMultipleEntities() throws JMSException {
    // setup
    Message message = mock(Message.class);
    when(message.getStringProperty(Broker.PROP_ACTION)).thenReturn(ACTION_TYPE.getStringRepresentation());
    when(message.getStringProperty(Broker.PROP_DOC_TYPE)).thenReturn(TypeNames.getInternalName(TYPE));
    when(message.getBooleanProperty(Broker.PROP_IS_MULTI_ENTITY)).thenReturn(true);
    when(consumer.receive(anyInt())).thenReturn(message);

    // action
    Action action = instance.receive();

    // verify
    assertThat(action, is(likeAction()//
        .withType(TYPE) //
        .withActionType(ACTION_TYPE) //
        .withId(null) //
        .withForMultiEntitiesFlag(true)));
  }

  @Test
  public void receiveReturnsNullWhenNoMessageCanBeReceived() throws JMSException {
    // setup
    Message nullMessage = null;
    when(consumer.receive(anyInt())).thenReturn(nullMessage);

    // action
    Action action = instance.receive();

    // verify
    assertThat(action, is(nullValue()));
  }
}
