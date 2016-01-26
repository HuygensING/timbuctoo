package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.DocumentType;
import nl.knaw.huygens.timbuctoo.model.Gender;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.TimbuctooQuery;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.server.rest.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;

public class WwDocumentSearchDescription implements SearchDescription {
  private static final List<String> SORTABLE_FIELDS = Lists.newArrayList(
      "dynamic_sort_title",
      "dynamic_k_modified",
      "dynamic_sort_creator");

  private static final List<String> FULL_TEXT_SEARCH_FIELDS = Lists.newArrayList(
      "dynamic_t_author_name",
      "dynamic_t_title",
      "dynamic_t_notes");

  private final PropertyParserFactory propertyParserFactory;
  private final PropertyDescriptorFactory propertyDescriptorFactory;

  private final String type = "wwdocument";

  public WwDocumentSearchDescription() {
    propertyDescriptorFactory = new PropertyDescriptorFactory();
    propertyParserFactory = new PropertyParserFactory();
  }

  @Override
  public List<String> getSortableFields() {
    return SORTABLE_FIELDS;
  }

  @Override
  public List<String> getFullTextSearchFields() {
    return FULL_TEXT_SEARCH_FIELDS;
  }

  @Override
  public TimbuctooQuery createQuery(SearchRequestV2_1 searchRequest) {
    return new TimbuctooQuery(this);
  }

  @Override
  public EntityRef createRef(Vertex vertex) {
    String id = propertyDescriptorFactory
        .getLocal(ID_DB_PROP, new PropertyParserFactory().getParser(String.class)).get(vertex);

    PropertyDescriptor authorNameDescriptor = propertyDescriptorFactory.getDerivedWithSeparator(
        "isCreatedBy",
        "wwperson_names",
        propertyParserFactory.getParser(PersonNames.class),
        "; ");
    PropertyDescriptor authorTempNameDescriptor = propertyDescriptorFactory.getDerivedWithSeparator(
        "isCreatedBy",
        "wwperson_tempName",
        propertyParserFactory.getParser(String.class),
        "; ");

    String authorNames = propertyDescriptorFactory
        .getComposite(authorNameDescriptor, authorTempNameDescriptor).get(vertex);

    String title = propertyDescriptorFactory.getLocal("wwdocument_title",
        propertyParserFactory.getParser(String.class)).get(vertex);

    String date = propertyDescriptorFactory.getLocal("date",
        propertyParserFactory.getParser(Datable.class)).get(vertex);

    EntityRef ref = new EntityRef(type, id);
    ref.setDisplayName(getDisplayName(vertex, title, authorNames, date));

    Map<String, Object> data = Maps.newHashMap();
    data.put("_id", id);
    data.put("authorName", authorNames);
    data.put("date", date);
    data.put("title", title);

    data.put("authorGender", propertyDescriptorFactory.getDerived(
        "isCreatedBy",
        "wwperson_gender",
        propertyParserFactory.getParser(Gender.class))
        .get(vertex));

    data.put("documentType", propertyDescriptorFactory
        .getLocal("wwdocument_documentType", propertyParserFactory.getParser(DocumentType.class))
        .get(vertex));

    data.put("modified_date",
        propertyDescriptorFactory.getLocal("modified", propertyParserFactory.getParser(Change.class)).get(vertex));

    ref.setData(data);

    return ref;
  }

  private String getDisplayName(Vertex vertex, String title, String authorNames, String date) {
    StringBuilder displayNameBuilder = new StringBuilder();

    if (authorNames != null) {
      displayNameBuilder.append(authorNames).append(" - ");
    }

    if (title == null && date == null && authorNames == null) {
      title = "(empty)";
    }
    displayNameBuilder.append(title);

    if (date != null) {
      displayNameBuilder.append(" (").append(date).append(")");
    }


    return displayNameBuilder.toString();
  }

  @Override
  public GraphTraversal<Vertex, Vertex> filterByType(GraphTraversal<Vertex, Vertex> vertices) {
    return vertices.filter(x -> ((String) x.get().property("types").value()).contains(type));
  }

  public String getType() {
    return type;
  }
}
