package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphLegacyStorageWrapper;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQueryFactory;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class TinkerPopDBIntegrationTestHelper implements DBIntegrationTestHelper {

  private Graph graph;
  private TimbuctooQueryFactory tinkerPopQueryFactory;

  @Override
  public void startCleanDB() throws Exception {
    graph = new TinkerGraph();
    tinkerPopQueryFactory = new TimbuctooQueryFactory();
  }

  @Override
  public void stopDB() {
    graph.shutdown();
  }

  @Override
  public Storage createStorage(TypeRegistry typeRegistry) throws ModelException {
    return new GraphLegacyStorageWrapper(new TinkerPopStorage(graph, typeRegistry), tinkerPopQueryFactory);
  }

}
