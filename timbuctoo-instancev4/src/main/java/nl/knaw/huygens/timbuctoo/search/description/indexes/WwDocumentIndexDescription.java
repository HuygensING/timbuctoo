package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.property.WwDocumentDisplayNameDescriptor;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

public class WwDocumentIndexDescription extends AbstractFulltextIndexDescription {
  private final PropertyDescriptor displayNameDescriptor;

  public WwDocumentIndexDescription() {
    displayNameDescriptor = new WwDocumentDisplayNameDescriptor();
  }

  @Override
  public void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    final Map<String, String> fields = Maps.newHashMap();
    final String displayName = displayNameDescriptor.get(vertex);

    fields.put("displayName", displayName == null ? "" : displayName);

    addToFulltextIndex(vertex, graphDatabase, "wwdocuments", fields);
  }

  @Override
  public void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    removeFromFulltextIndex(vertex, graphDatabase, "wwdocuments");
  }
}
