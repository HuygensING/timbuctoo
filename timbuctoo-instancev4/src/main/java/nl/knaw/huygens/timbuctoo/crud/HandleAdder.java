package nl.knaw.huygens.timbuctoo.crud;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.logmarkers.Logmarkers;
import nl.knaw.huygens.timbuctoo.queued.ActiveMqExecutor;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.slf4j.Logger;

import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowConsumer;

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
      String persistentUrl = manager.getPersistentURL(manager.persistURL(params.getUrl().toString()));
      wrapper.getGraph().traversal().V()
        .has("tim_id", params.getVertexId().toString())
        .has("rev", params.getRev())
        .forEachRemaining(vertex -> {
          vertex.property("pid", persistentUrl);
        });
      wrapper.getGraph().tx().commit();
    } catch (PersistenceException e) {
      LOG.error(Logmarkers.serviceUnavailable, "Could not create handle", e);
      if (params.getRetries() < 5) {
        add(new HandleAdderParameters(params.getVertexId(), params.getRev(), params.getUrl(), params.getRetries() + 1));
      }
    }
  }

  public void add(HandleAdderParameters params) {
    activeMqExecutor.add(params);
  }
}
