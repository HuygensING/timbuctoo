package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.google.common.collect.Maps;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

public class WwKeywordIndexDescription extends AbstractFulltextIndexDescription {
  @Override
  public void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    final Map<String, String> fields = Maps.newHashMap();

    fields.put("displayName", (String) vertex.property("wwkeyword_value").value());
    fields.put("type", (String) vertex.property("wwkeyword_type").value());

    addToFulltextIndex(vertex, graphDatabase, "wwkeywords", fields);

  }

  @Override
  public void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    removeFromFulltextIndex(vertex, graphDatabase, "wwkeywords");
  }
}
