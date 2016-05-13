package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.List;
import java.util.Map;

public class WwCollectiveIndexDescription extends AbstractFulltextIndexDescription {
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

    fields.put("displayName", (String) vertex.property("wwcollective_name").value());

    addToFulltextIndex(vertex, graphDatabase, "wwcollectives", fields);

  }

  @Override
  public void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    removeFromFulltextIndex(vertex, graphDatabase, "wwcollectives");
  }
}
