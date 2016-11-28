package nl.knaw.huygens.timbuctoo.search.description.indexes;


import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.model.TempName;
import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

class WwPersonIndexDescription extends AbstractFulltextIndexDescription {

  private final PropertyDescriptor displayNameDescriptor;

  public WwPersonIndexDescription() {

    final PropertyDescriptorFactory propertyDescriptorFactory =
            new PropertyDescriptorFactory(new PropertyParserFactory());

    displayNameDescriptor = propertyDescriptorFactory.getComposite(
            propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
            propertyDescriptorFactory.getLocal("wwperson_tempName", TempName.class));
  }


  @Override
  public void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    final Map<String, String> fields = Maps.newHashMap();
    final String displayName = displayNameDescriptor.get(vertex);

    fields.put("displayName", displayName == null ? "" : displayName);

    addToFulltextIndex(vertex, graphDatabase, "wwpersons", fields);
  }

  @Override
  public void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    removeFromFulltextIndex(vertex, graphDatabase, "wwpersons");
  }

}
