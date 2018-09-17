package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import com.kjetland.dropwizard.activemq.ActiveMQSender;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.dto.EntityLookup;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.queued.ActiveMqQueueCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class RedirectionService {
  private static final Logger LOG = LoggerFactory.getLogger(RedirectionService.class);
  private final ActiveMQSender sender;
  protected TransactionEnforcer transactionEnforcer;

  public RedirectionService(ActiveMQBundle activeMqBundle, String queueName) {
    ActiveMqQueueCreator<HandleAdderParameters> queueCreator = new ActiveMqQueueCreator<>(
      HandleAdderParameters.class,
      queueName,
      activeMqBundle
    );
    queueCreator.registerReceiver(this::actualExecution);
    this.sender = queueCreator.createSender();
  }

  private void actualExecution(HandleAdderParameters params) {
    try {
      savePid(params);
    } catch (PersistenceException | URISyntaxException e) {
      LOG.error(Logmarkers.serviceUnavailable, "Could not create handle", e);
      if (params.getRetries() < 5) {
        LOG.warn(String.format("Re-adding %s%s job to the queue for '%s' '%s'",
          params.getRetries() + 1, getOrdinalSuffix(params.getRetries() + 1),
          params.getEntityLookup(),
          params.getUrlToRedirectTo()
        ));
        this.sender.send(params.nextTry());
      }
    }
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

  protected abstract void savePid(HandleAdderParameters params) throws PersistenceException, URISyntaxException;

  public final void add(URI uriToRedirectTo, EntityLookup entityLookup) {
    if (transactionEnforcer == null) {
      throw new IllegalStateException("init() must be called before you can add items");
    }
    this.sender.send(new HandleAdderParameters(uriToRedirectTo, entityLookup));
  }

  public final void init(TransactionEnforcer transactionEnforcer) {
    this.transactionEnforcer = transactionEnforcer;
  }
}
