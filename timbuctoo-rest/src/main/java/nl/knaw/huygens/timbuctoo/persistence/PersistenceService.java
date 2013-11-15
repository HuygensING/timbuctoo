package nl.knaw.huygens.timbuctoo.persistence;

import javax.jms.JMSException;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.ConsumerService;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class PersistenceService extends ConsumerService implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(PersistenceService.class);
  private final PersistenceWrapper persistenceWrapper;
  private final StorageManager storageManager;

  @Inject
  public PersistenceService(Broker broker, PersistenceWrapper persistenceWrapper, StorageManager storageManager) throws JMSException {
    super(broker, Broker.PERSIST_QUEUE, "PersistenceService");
    this.persistenceWrapper = persistenceWrapper;
    this.storageManager = storageManager;
  }

  @Override
  protected void executeAction(Action action) {
    try {
      switch (action.getActionType()) {
      case ADD:
      case MOD:
        Class<? extends Entity> type = action.getType();
        if (TypeRegistry.isDomainEntity(type)) {
          String pid = persistenceWrapper.persistObject(type, action.getId());
          storageManager.setPID(TypeRegistry.toDomainEntity(type), action.getId(), pid);
        } else {
          LOG.error("Not a domain entitiy: {}", type.getSimpleName());
        }
        break;
      case DEL:
        LOG.debug("Ignoring action {}", action);
        break;
      default:
        LOG.warn("Unexpected action {}", action);
        break;
      }
    } catch (PersistenceException e) {
      LOG.error("Persisting {} with id {} went wrong", action.getType(), action.getId());
      LOG.error("exception", e);
    }
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

}
