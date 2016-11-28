package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.google.common.collect.Maps;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

public class WwLanguageIndexDescription extends AbstractFulltextIndexDescription {
  @Override
  public void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    final Map<String, String> fields = Maps.newHashMap();

    fields.put("displayName", (String) vertex.property("wwlanguage_name").value());

    addToFulltextIndex(vertex, graphDatabase, "wwlanguages", fields);

  }

  @Override
  public void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    removeFromFulltextIndex(vertex, graphDatabase, "wwlanguages");
  }
}
