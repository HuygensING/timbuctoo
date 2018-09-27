package nl.knaw.huygens.timbuctoo.v5.redirectionservice.bitly;


import nl.knaw.huygens.timbuctoo.v5.queue.QueueCreator;
import nl.knaw.huygens.timbuctoo.v5.queue.QueueManager;
import nl.knaw.huygens.timbuctoo.v5.queue.QueueSender;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class BitlyServiceTest {
  @Test
  public void retrieveBitlyUriSuccessfullyRetrievesShortenedUri() {
    QueueManager queueManager = mock(QueueManager.class);
    QueueCreator queueCreator = new NonQueueQueueCreator();
    given(queueManager.createQueue(any(),anyString())).willReturn(queueCreator);
    BitlyService bitlyService = new BitlyService(queueManager);

    String shortenedUri = bitlyService.retrieveBitlyUri("https://example.com/");

    assertThat(shortenedUri, startsWith("http://bit.ly/"));
  }

  private static class NonQueueQueueCreator<T> implements QueueCreator<T> {
    List<QueueSender> queueSenderList = new ArrayList<>();
    List<Consumer> receiverList = new ArrayList<>();

    @Override
    public void registerReceiver(Consumer consumer) {
      receiverList.add(consumer);
    }

    @Override
    public QueueSender createSender() {
      QueueSender queueSender = new NonQueueQueueSender(receiverList);
      queueSenderList.add(queueSender);
      return queueSender;
    }

    private static class NonQueueQueueSender implements QueueSender {
      private final List<Consumer> receiverList;

      public NonQueueQueueSender(List<Consumer> receiverList) {
        this.receiverList = receiverList;
      }

      @Override
      public void send(Object object) {
        receiverList.forEach(receiver -> receiver.accept(object));
      }
    }


  }
}
