package nl.knaw.huygens.timbuctoo.search.description.indexes;

import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import java.util.Map;

public abstract class AbstractFulltextIndexDescription implements IndexDescription {

  private Index<Node> index;

  private Index<Node> getIndex(GraphDatabaseService graphDatabase, String indexName, Map<String, String> indexConfig) {
    if (index == null) {
      final IndexManager indexManager = graphDatabase.index();
      if (indexConfig == null) {
        index = indexManager.forNodes(indexName);
      } else {
        index = indexManager.forNodes(indexName, indexConfig);
      }
    }
    return index;
  }

  protected void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase, String indexName,
                                    Map<String, String> fields) {

    final Map<String, String> indexConfig = MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext");
    final Index<Node> index = getIndex(graphDatabase, indexName, indexConfig);
    final String timId = (String) vertex.property("tim_id").value();

    removeFromFulltextIndex(vertex, graphDatabase, indexName);

    long id = (long) vertex.id();
    Node neo4jNode = graphDatabase.getNodeById(id);
    for (Map.Entry<String, String> field : fields.entrySet()) {
      index.add(neo4jNode, field.getKey(), field.getValue());
    }
    index.add(neo4jNode, "tim_id", timId);
  }

  protected void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase, String indexName) {
    final Index<Node> index = getIndex(graphDatabase, indexName, null);
    final String timId = (String) vertex.property("tim_id").value();

    IndexHits<Node> hits = index.get("tim_id", timId);
    while (hits.hasNext()) {
      Node node = hits.next();
      index.remove(node);
    }
  }

}
