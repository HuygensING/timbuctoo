package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

public class WwKeywordIndexDescription extends AbstractFulltextIndexDescription {
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
    final Map<String, String> fields = Maps.newHashMap();

    fields.put("displayName", (String) vertex.property("wwkeyword_value").value());
    fields.put("type", (String) vertex.property("wwkeyword_type").value());

    addToFulltextIndex(vertex, graphDatabase, "wwkeywords", fields);

  }
}
