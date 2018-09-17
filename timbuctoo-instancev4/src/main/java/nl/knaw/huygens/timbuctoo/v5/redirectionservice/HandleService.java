package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.core.NotFoundException;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.TransactionState;
import nl.knaw.huygens.timbuctoo.core.dto.EntityLookup;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class HandleService extends RedirectionService {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HandleService.class);
  public static final String HANDLE_QUEUE = "pids";

  private final PersistenceManager manager;
  private TransactionEnforcer transactionEnforcer;

  public HandleService(PersistenceManager manager, ActiveMQBundle activeMqBundle) {
    super(activeMqBundle, HANDLE_QUEUE);
    this.manager = manager;
    this.transactionEnforcer = null;
  }

  protected void savePid(HandleAdderParameters params) throws PersistenceException, URISyntaxException {
    URI uri = params.getUrlToRedirectTo();
    LOG.info(String.format("Retrieving persistent url for '%s'", uri));
    String persistentId = (manager.persistURL(uri.toString()));
    URI persistentUrl = new URI(manager.getPersistentURL(persistentId));

    transactionEnforcer.execute(timbuctooActions -> {
        try {
          timbuctooActions.addPid(persistentUrl, params.getEntityLookup());
          LOG.info("committed pid");
          return TransactionState.commit();
        } catch (NotFoundException e) {
          LOG.warn("Entity for entityLookup '{}' cannot be found", params.getEntityLookup());
          try {
            manager.deletePersistentId(persistentId);
          } catch (PersistenceException e1) {
            LOG.error("Cannot remove handle with id '{}'", persistentId);
          }
          return TransactionState.rollback();
        }
      }
    );
  }

}
