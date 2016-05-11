package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class AutocompleteLuceneIndexDatabaseMigration implements DatabaseMigration {

  private Index<Node> keywordIndex;
  private Index<Node> documentIndex;
  private Index<Node> personIndex;

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void generateIndexes(Neo4jGraph neo4jGraph, Transaction transaction) {
    // TODO?
  }

  @Override
  public void beforeMigration(TinkerpopGraphManager graphManager) {
    final IndexManager indexManager = graphManager.getGraphDatabase().index();
    final Map<String, String> indexConfig = MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext");
    keywordIndex = indexManager.forNodes("wwkeywords", indexConfig);
    documentIndex = indexManager.forNodes("wwdocuments", indexConfig);
    personIndex = indexManager.forNodes("wwpersons", indexConfig);
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    List<String> types = Arrays.asList(getEntityTypesOrDefault(vertex));

    List<IndexDescription> indexDescriptions = new IndexDescriptionFactory().getIndexersForTypes(types);
    for (IndexDescription indexDescription : indexDescriptions) {
      indexDescription.addToFulltextIndex(vertex, personIndex);
    }
  }
}
