package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import java.util.List;
import java.util.Map;

public class WwKeywordIndexDescription implements IndexDescription {
  @Override
  public List<IndexerSortFieldDescription> getSortFieldDescriptions() {
    return Lists.newArrayList();
  }

  @Override
  public void addIndexedSortProperties(Vertex vertex) {
    // No custom sort fields for keywords necessary
  }

  @Override
  public void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    final IndexManager indexManager = graphDatabase.index();
    final Map<String, String> indexConfig = MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext");
    final Index<Node> index = indexManager.forNodes("wwkeywords", indexConfig);
    final String timId = (String) vertex.property("tim_id").value();

    IndexHits<Node> hits = index.get("tim_id", timId);
    while (hits.hasNext()) {
      Node node = hits.next();
      index.remove(node);
    }

    long id = (long) vertex.id();
    Node neo4jNode = graphDatabase.getNodeById(id);
    index.add(neo4jNode, "displayName", vertex.property("wwkeyword_value").value());
    index.add(neo4jNode, "type", vertex.property("wwkeyword_type").value());
    index.add(neo4jNode, "tim_id", timId);
  }
}
