package nl.knaw.huygens.timbuctoo.crud;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.queued.ActiveMqExecutor;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class HandleAdder {

  private final ActiveMqExecutor<HandleAdderParameters> activeMqExecutor;
  private final GraphWrapper wrapper;
  private final PersistenceManager manager;

  public HandleAdder(ActiveMQBundle mq, String queuename, GraphWrapper wrapper, PersistenceManager manager) {
    this.wrapper = wrapper;
    this.manager = manager;
    this.activeMqExecutor = new ActiveMqExecutor<>(mq, queuename, this::create, HandleAdderParameters.class);
  }

  public void create(HandleAdderParameters params) {
    Vertex vertex = wrapper.getGraph().vertices(params.getVertexId()).next();
    try {
      vertex.property("pid", manager.getPersistentURL(manager.persistURL(params.getUrl().toString())));
    } catch (PersistenceException e) {
      //log
      if (params.getRetries() < 5) {
        add(new HandleAdderParameters(params.getVertexId(), params.getUrl(), params.getRetries() + 1));
      }
    }
  }

  public void add(HandleAdderParameters params) {
    activeMqExecutor.add(params);
  }
}
