package nl.knaw.huygens.timbuctoo.handle;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.PersistentUrlCreator;
import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.database.TransactionState;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

class HandleAdder {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HandleAdder.class);

  private final GraphWrapper wrapper;
  private final PersistenceManager manager;
  private final UrlGenerator handleUri;
  private final PersistentUrlCreator retryPersistentUrlCreator;
  private final TransactionEnforcer transactionEnforcer;

  public HandleAdder(GraphWrapper wrapper, PersistenceManager manager, UrlGenerator handleUri,
                     PersistentUrlCreator retryPersistentUrlCreator, TransactionEnforcer transactionEnforcer) {
    this.wrapper = wrapper;
    this.manager = manager;
    this.handleUri = handleUri;
    this.retryPersistentUrlCreator = retryPersistentUrlCreator;
    this.transactionEnforcer = transactionEnforcer;
  }

  public void create(HandleAdderParameters params) {
    UUID id = params.getVertexId();
    int rev = params.getRev();
    try {
      URI uri = handleUri.apply(params.getCollectionName(), id, rev);
      LOG.info(String.format("Retrieving persistent url for '%s' '%s' '%s'",
        id, rev, uri));
      String persistentId = (manager.persistURL(uri.toString()));
      URI persistentUrl = new URI(manager.getPersistentURL(persistentId));

      transactionEnforcer.executeTimbuctooAction(timbuctooActions -> {
          try {
            timbuctooActions.addPid(id, rev, persistentUrl);
            LOG.info("committed pid");
            return TransactionState.commit();
          } catch (NotFoundException e) {
            LOG.warn("Entity with id '{}' and revision '{}' cannot be found", id, rev);
            try {
              manager.deletePersistentId(persistentId);
            } catch (PersistenceException e1) {
              LOG.error("Cannot remove handle with id '{}'", persistentId);
            }
            return TransactionState.rollback();
          }
        }
      );
    } catch (PersistenceException | URISyntaxException e) {
      LOG.error(Logmarkers.serviceUnavailable, "Could not create handle", e);
      if (params.getRetries() < 5) {
        add(new HandleAdderParameters(params.getCollectionName(), id, rev,
          params.getRetries() + 1));
      }
    }
  }

  private void add(HandleAdderParameters params) {
    LOG.info(String.format("Adding %s%s job to the queue for '%s' '%s' '%s'",
      params.getRetries() + 1, getOrdinalSuffix(params.getRetries() + 1),
      params.getVertexId(),
      params.getRev(),
      handleUri.apply(params.getCollectionName(), params.getVertexId(), params.getRev())
    ));
    retryPersistentUrlCreator.add(params);
  }

  // gogo gadgetstackoverflow
  private String getOrdinalSuffix(int value) {
    int hunRem = value % 100;
    int tenRem = value % 10;

    if (hunRem - tenRem == 10) {
      return "th";
    }
    switch (tenRem) {
      case 1:
        return "st";
      case 2:
        return "nd";
      case 3:
        return "rd";
      default:
        return "th";
    }
  }

}
