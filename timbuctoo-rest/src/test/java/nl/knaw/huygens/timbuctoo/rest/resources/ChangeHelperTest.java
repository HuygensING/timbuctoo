package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import javax.jms.JMSException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChangeHelperTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;

  @Test
  public void updatePidsSendsAnUpdateAllMessageOnWithThePersistenceProducer() throws JMSException {
    // setup
    Broker broker = mock(Broker.class);
    Producer producer = mock(Producer.class);
    when(broker.getProducer(ChangeHelper.PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE)).thenReturn(producer);

    Action action = Action.multiUpdateActionFor(TYPE);

    ChangeHelper instance = new ChangeHelper(broker, mock(TypeRegistry.class));

    // action
    instance.sendUpdatePIDMessage(TYPE);

    // verify
    verify(producer).send(action);

  }


}
