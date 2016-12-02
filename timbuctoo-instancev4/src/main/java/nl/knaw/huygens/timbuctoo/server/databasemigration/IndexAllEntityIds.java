package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.GremlinEntityFetcher;
import nl.knaw.huygens.timbuctoo.database.changelistener.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.Neo4jIndexHandler;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class IndexAllEntityIds implements DatabaseMigration {
  public static final ArrayList<String> TYPES_TO_IGNORE = Lists.newArrayList("relationtype", "searchresult");
  private static final Logger LOG = LoggerFactory.getLogger(IndexAllEntityIds.class);

  @Override
  public void execute(TinkerPopGraphManager graphManager) throws IOException {
    DataStoreOperations dataStoreOperations = new DataStoreOperations(
      graphManager,
      new DeafListener(),
      new GremlinEntityFetcher(),
      null,
      new Neo4jIndexHandler(graphManager)
    );

    Vres vres = dataStoreOperations.loadVres();

    Neo4jIndexHandler indexHandler = new Neo4jIndexHandler(graphManager);
    ObjectMapper mapper = new ObjectMapper();
    GraphTraversalSource traversalSource = graphManager.getGraph().traversal();
    traversalSource
      .V()
      .has("types") //only valid entities
      .forEachRemaining(vertex -> {
        try {
          String[] types = mapper.readValue(vertex.<String>value("types"), String[].class);
          for (String type : types) {
            if (TYPES_TO_IGNORE.contains(type)) {
              continue;
            }
            if (vres.getCollectionForType(type).isPresent()) {
              VertexProperty<String> timIdProp = vertex.property("tim_id");
              if (timIdProp.isPresent()) {
                try {
                  UUID timId = UUID.fromString(timIdProp.value());
                  indexHandler.insertIntoIdIndex(timId, vertex);
                } catch (IllegalArgumentException e) {
                  // This exception should not happen, but we do not want our migration to fail on it.
                  LOG.error("'{}' is not a valid id", timIdProp.value());
                }
              } else {
                LOG.error("Vertex with id '{}' has no 'tim_id' property", vertex.id());
              }

            } else {
              LOG.error("'{}' is not a known entity type.", type);
            }
          }
        } catch (IOException e) {
          LOG.error("And exception occurred while indexing vertex with vertex id '{}'.", vertex.id());
        }
      });
  }

  private class DeafListener implements ChangeListener {

    @Override
    public void onCreate(Collection collection, Vertex vertex) {

    }

    @Override
    public void onPropertyUpdate(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {

    }

    @Override
    public void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {

    }

    @Override
    public void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {

    }
  }
}
