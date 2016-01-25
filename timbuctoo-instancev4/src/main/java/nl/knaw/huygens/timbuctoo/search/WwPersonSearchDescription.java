package nl.knaw.huygens.timbuctoo.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.Gender;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.server.rest.search.SearchRequestV2_1;
import nl.knaw.huygens.timbuctoo.search.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.propertyparser.PropertyParserFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class WwPersonSearchDescription {
  public static final Logger LOG = LoggerFactory.getLogger(WwPersonSearchDescription.class);
  private final PropertyParserFactory propertyParserFactory;
  private final PropertyDescriptorFactory propertyDescriptorFactory;
  private ObjectMapper objectMapper;

  private static final List<String> SORTABLE_FIELDS = Lists.newArrayList(
    "dynamic_k_modified",
    "dynamic_k_birthDate",
    "dynamic_sort_name",
    "dynamic_k_deathDate");

  private static final List<String> FULL_TEXT_SEARCH_FIELDS = Lists.newArrayList(
    "dynamic_t_tempspouse",
    "dynamic_t_notes",
    "dynamic_t_name");
  public static final String ID_DB_PROP = "tim_id";
  private final String type = "wwperson";

  public WwPersonSearchDescription() {
    objectMapper = new ObjectMapper();
    propertyParserFactory = new PropertyParserFactory();
    propertyDescriptorFactory = new PropertyDescriptorFactory();
  }

  public List<String> getSortableFields() {
    return SORTABLE_FIELDS;
  }

  public List<String> getFullTextSearchFields() {
    return FULL_TEXT_SEARCH_FIELDS;
  }

  public TimbuctooQuery createQuery(SearchRequestV2_1 searchRequest) {
    return new TimbuctooQuery(this);
  }

  public EntityRef createRef(Vertex vertex) {
    String id =
      propertyDescriptorFactory.getLocal(ID_DB_PROP, new PropertyParserFactory().getParser(String.class)).get(vertex);

    EntityRef ref = new EntityRef(type, id);

    PropertyDescriptor descriptor = propertyDescriptorFactory.getComposite(
      propertyDescriptorFactory.getLocal("wwperson_names", propertyParserFactory.getParser(PersonNames.class)),
      propertyDescriptorFactory.getLocal("wwperson_tempName", propertyParserFactory.getParser(String.class)));

    String displayName = descriptor.get(vertex);
    ref.setDisplayName(displayName);

    Map<String, Object> data = Maps.newHashMap();
    data.put("_id", id);
    data.put("name", displayName);

    data.put("birthDate", propertyDescriptorFactory
      .getLocal("wwperson_birthDate", propertyParserFactory.getParser(Datable.class)).get(vertex));
    data.put("deathDate", propertyDescriptorFactory
      .getLocal("wwperson_deathDate", propertyParserFactory.getParser(Datable.class)).get(vertex));
    data.put("gender", propertyDescriptorFactory
      .getLocal("wwperson_gender", propertyParserFactory.getParser(Gender.class)).get(vertex));

    data.put("modified_date",
      propertyDescriptorFactory.getLocal("modified", propertyParserFactory.getParser(Change.class)).get(vertex));

    data.put("residenceLocation", propertyDescriptorFactory.getDerived(
      "hasResidenceLocation",
      "names",
      propertyParserFactory
        .getParser(LocationNames.class)).get(vertex));

    ref.setData(data);

    return ref;
  }

  public GraphTraversal<Vertex, Vertex> filterByType(GraphTraversal<Vertex, Vertex> vertices) {
    return vertices.filter(x -> ((String) x.get().property("types").value()).contains(type));
  }

  String getType() {
    return type;
  }

}
