package nl.knaw.huygens.timbuctoo.search.description.indexes;


import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

public class WwLocationIndexDescription extends AbstractFulltextIndexDescription {

  private final PropertyDescriptor displayNameDescriptor;

  public WwLocationIndexDescription() {
    final PropertyDescriptorFactory propertyDescriptorFactory =
            new PropertyDescriptorFactory(new PropertyParserFactory());

    displayNameDescriptor = propertyDescriptorFactory.getLocal("names", LocationNames.class);
  }


  @Override
  public void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    final Map<String, String> fields = Maps.newHashMap();
    final String displayName = displayNameDescriptor.get(vertex);

    fields.put("displayName", displayName == null ? "" : displayName);

    addToFulltextIndex(vertex, graphDatabase, "wwlocations", fields);

  }

  @Override
  public void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    removeFromFulltextIndex(vertex, graphDatabase, "wwlocations");
  }
}
