package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.GremlinEntityFetcher;
import nl.knaw.huygens.timbuctoo.database.dto.DisplayNameHelper;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.Neo4jIndexHandler;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopOperations;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class IndexAllTheDisplaynames implements DatabaseMigration {

  public static final ArrayList<String> TYPES_TO_IGNORE = Lists.newArrayList("relationtype", "searchresult");
  private static final Logger LOG = LoggerFactory.getLogger(IndexAllTheDisplaynames.class);

  @Override
  public void execute(TinkerPopGraphManager graphManager) throws IOException {
    TinkerPopOperations dataStoreOperations = new TinkerPopOperations(
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
            Optional<Collection> collectionOpt = vres.getCollectionForType(type);
            if (collectionOpt.isPresent()) {
              Collection collection = collectionOpt.get();
              DisplayNameHelper.getDisplayname(traversalSource, vertex, collection)
                               .ifPresent(displayName -> {
                                 indexHandler.upsertIntoQuickSearchIndex(collection, displayName, vertex);
                               });
            } else {
              LOG.error("'{}' is not a known entity type.", type);
            }
          }
        } catch (IOException e) {
          LOG.error("And exception occurred while indexing vertex with vertex id '{}'.", vertex.id());
        }
      });
  }
}
