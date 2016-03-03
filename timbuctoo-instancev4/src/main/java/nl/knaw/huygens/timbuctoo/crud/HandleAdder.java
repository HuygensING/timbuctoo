package nl.knaw.huygens.timbuctoo.crud;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.queued.ActiveMqExecutor;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.slf4j.Logger;

public class HandleAdder {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HandleAdder.class);

  private final ActiveMqExecutor<HandleAdderParameters> activeMqExecutor;
  private final GraphWrapper wrapper;
  private final PersistenceManager manager;

  public HandleAdder(ActiveMQBundle mq, String queuename, GraphWrapper wrapper, PersistenceManager manager) {
    this.wrapper = wrapper;
    this.manager = manager;
    this.activeMqExecutor = new ActiveMqExecutor<>(mq, queuename, this::create, HandleAdderParameters.class);
  }

  public void create(HandleAdderParameters params) {
    try {
      LOG.info(String.format("Retrieving persistent url for '%s' '%s' '%s'",
        params.getVertexId(), params.getRev(), params.getUrl()));
      String persistentUrl = manager.getPersistentURL(manager.persistURL(params.getUrl().toString()));
      wrapper.getGraph().traversal().V()
        .has("tim_id", params.getVertexId().toString())
        .has("rev", params.getRev())
        .forEachRemaining(vertex -> {
          LOG.info("Setting pid for " + vertex.id() + " to " + persistentUrl);
          vertex.property("pid", persistentUrl);
        });
      wrapper.getGraph().tx().commit();
      LOG.info("committed pid");
    } catch (PersistenceException e) {
      LOG.error(Logmarkers.serviceUnavailable, "Could not create handle", e);
      if (params.getRetries() < 5) {
        add(new HandleAdderParameters(params.getVertexId(), params.getRev(), params.getUrl(), params.getRetries() + 1));
      }
    }
  }

  public void add(HandleAdderParameters params) {
    LOG.info(String.format("Adding %s%s job to the queue for '%s' '%s' '%s'",
      params.getRetries() + 1, getOrdinalSuffix(params.getRetries() + 1),
      params.getVertexId(),
      params.getRev(),
      params.getUrl())
    );
    activeMqExecutor.add(params);
  }

  // gogo gadgetstackoverflow
  private static String getOrdinalSuffix( int value ) {
    int hunRem = value % 100;
    int tenRem = value % 10;

    if ( hunRem - tenRem == 10 ) {
      return "th";
    }
    switch ( tenRem ) {
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
