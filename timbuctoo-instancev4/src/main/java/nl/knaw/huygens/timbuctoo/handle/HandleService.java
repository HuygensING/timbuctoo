package nl.knaw.huygens.timbuctoo.handle;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import com.kjetland.dropwizard.activemq.ActiveMQSender;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.PersistentUrlCreator;
import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.queued.ActiveMqQueueCreator;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

public class HandleService {

  private final TinkerpopGraphManager graphManager;
  private final PersistenceManager persistenceManager;
  private final UrlGenerator handleUri;
  private HandleAdder handleAdder;
  private final ActiveMqQueueCreator<HandleAdderParameters> queueCreator;

  public HandleService(ActiveMQBundle activeMqBundle, String handleQueue, TinkerpopGraphManager graphManager,
                       PersistenceManager persistenceManager, UrlGenerator handleUri) {
    this.queueCreator = new ActiveMqQueueCreator<>(HandleAdderParameters.class, handleQueue, activeMqBundle);
    this.graphManager = graphManager;
    this.persistenceManager = persistenceManager;
    this.handleUri = handleUri;
  }

  public PersistentUrlCreator newHandleCreator() {
    ActiveMQSender sender = queueCreator.createSender();
    return sender::send;
  }

  public void start(TransactionEnforcer transactionEnforcer) {
    handleAdder = new HandleAdder(graphManager, persistenceManager, handleUri, newHandleCreator(), transactionEnforcer);
    queueCreator.registerReceiver(handleAdder::create);
  }

  public boolean isStarted() {
    return handleAdder != null;
  }
}
