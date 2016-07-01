package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class HuygensIngConfigToDatabaseMigration implements DatabaseMigration {
  private static final Logger LOG = LoggerFactory.getLogger(HuygensIngConfigToDatabaseMigration.class);

  private final Vres mappings;
  private Map<String, Map<String, String>> keywordTypes;

  public HuygensIngConfigToDatabaseMigration(Vres mappings, Map<String, Map<String, String>> keywordTypes) {

    this.mappings = mappings;
    this.keywordTypes = keywordTypes;
  }

  @Override
  public void beforeMigration(GraphWrapper graphManager) {

  }

  @Override
  public void execute(GraphWrapper graphWrapper) throws IOException {
    Graph graph = graphWrapper.getGraph();
    Transaction transaction = graph.tx();

    if (!transaction.isOpen()) {
      transaction.open();
    }

    mappings
      .getVres()
      .keySet()
      .stream()
      // Admin needs to come first, so all collection vertices can point to an existing
      // admin variant with the hasArchetype edge
      .sorted((nameA, nameB) -> nameA.equals("Admin") ? -1 : 1)
      .forEach((name) -> {
        final Vre vre = mappings.getVre(name);
        Vertex vreVertex = vre.save(graph, Optional.ofNullable(keywordTypes.get(name)));
        transaction.commit();

        // Add entities from each collection to the holder vertex using hasCollectionVertex relation
        vreVertex.vertices(Direction.OUT, Vre.HAS_COLLECTION_RELATION_NAME).forEachRemaining(collectionV -> {
          Vertex entityHolderVertex = collectionV
            .vertices(Direction.OUT, Collection.HAS_ENTITY_NODE_RELATION_NAME).next();

          int verticesAddedToCollection = graphWrapper
            .getCurrentEntitiesFor((String) collectionV.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME).value())
            .sideEffect((entityT) -> {
              entityHolderVertex.addEdge(Collection.HAS_ENTITY_RELATION_NAME, entityT.get());
            }).toList().size();

          LOG.info("Added {} entities to collection {}.", verticesAddedToCollection,
            collectionV.property(Collection.COLLECTION_NAME_PROPERTY_NAME).value());
        });

        transaction.commit();
      });

    transaction.close();
  }
}
