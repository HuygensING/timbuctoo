package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class AutocompleteLuceneIndexDatabaseMigration extends AbstractVertexMigration {

  private static final Logger LOG = LoggerFactory.getLogger(AutocompleteLuceneIndexDatabaseMigration.class);
  private GraphDatabaseService graphDatabase;

  @Override
  public void beforeMigration(GraphWrapper graphWrapper) {
    if (graphWrapper instanceof TinkerpopGraphManager) {
      graphDatabase = ((TinkerpopGraphManager) graphWrapper).getGraphDatabase();
    } else {
      LOG.error("GraphWrapper is not instance of TinkerpopGraphManager");
    }
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    if (graphDatabase == null) {
      throw new IOException("Graph database is not initialized.");
    }
    Boolean isLatest = vertex.property("isLatest").isPresent() ?
      (Boolean) vertex.property("isLatest").value() :
      false;

    if (isLatest) {
      List<String> types = Arrays.asList(getEntityTypesOrDefault(vertex));
      List<IndexDescription> indexDescriptions = new IndexDescriptionFactory().getIndexersForTypes(types);
      for (IndexDescription indexDescription : indexDescriptions) {
        indexDescription.addToFulltextIndex(vertex, graphDatabase);
      }
    }
  }

}
