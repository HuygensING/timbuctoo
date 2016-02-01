package nl.knaw.huygens.timbuctoo.queued;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

import java.util.function.Consumer;

public class ActiveMqExecutor<T> {

  private final ActiveMQSender sender;

  public ActiveMqExecutor(ActiveMQBundle mq, String queuename, Consumer<T> consumer, Class<? extends T> clazz) {
    this.sender = mq.createSender("queue:" + queuename, true);
    mq.registerReceiver(
      "queue:" + queuename,
      consumer::accept,
      clazz,
      false
    );
  }

  public void add(T parameters) {
    sender.send(parameters);
  }

}
