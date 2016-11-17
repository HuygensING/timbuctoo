package nl.knaw.huygens.timbuctoo.queued;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

import java.util.function.Consumer;

public class ActiveMqQueueCreator<T> {
  private final Class<T> messageType;
  private final String queueName;
  private final ActiveMQBundle mq;

  public ActiveMqQueueCreator(Class<T> messageType, String queueName, ActiveMQBundle mq) {
    this.messageType = messageType;
    this.queueName = queueName;
    this.mq = mq;
  }

  public void registerReceiver(Consumer<T> consumer) {
    mq.registerReceiver(
      "queue:" + queueName,
      consumer::accept,
      messageType,
      false
    );
  }

  public ActiveMQSender createSender() {
    return mq.createSender("queue:" + queueName, true);
  }

}
