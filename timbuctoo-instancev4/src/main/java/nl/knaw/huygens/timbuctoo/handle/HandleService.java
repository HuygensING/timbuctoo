package nl.knaw.huygens.timbuctoo.handle;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import com.kjetland.dropwizard.activemq.ActiveMQSender;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.HandleCreator;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

public class HandleService {

  private final ActiveMQBundle activeMqBundle;
  private final String handleQueue;
  private final TinkerpopGraphManager graphManager;
  private final PersistenceManager persistenceManager;
  private final UrlGenerator handleUri;
  private HandleAdder handleAdder;

  public HandleService(ActiveMQBundle activeMqBundle, String handleQueue, TinkerpopGraphManager graphManager,
                       PersistenceManager persistenceManager, UrlGenerator handleUri) {

    this.activeMqBundle = activeMqBundle;
    this.handleQueue = handleQueue;
    this.graphManager = graphManager;
    this.persistenceManager = persistenceManager;
    this.handleUri = handleUri;
  }

  public HandleCreator newHandleCreator() {
    // TODO Let ActiveMqExecutor create the sender and receiver.
    ActiveMQSender sender = activeMqBundle.createSender("queue:" + handleQueue, true);
    return sender::send;
  }

  public void start() {
    handleAdder = new HandleAdder(activeMqBundle, handleQueue, graphManager, persistenceManager, handleUri);
  }

  public boolean isStarted() {
    return handleAdder != null;
  }
}
