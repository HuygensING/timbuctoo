package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.DocumentType;
import nl.knaw.huygens.timbuctoo.model.Gender;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.search.description.sort.SortDescription;

import java.util.List;
import java.util.Map;

public class WwDocumentSearchDescription extends AbstractSearchDescription implements SearchDescription {
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
  private final Map<String, PropertyDescriptor> dataDescriptors;
  private final List<FacetDescription> facetDescriptions;
  private final PropertyDescriptor idDescriptor;
  private final PropertyDescriptor displayNameDescriptor;

  public WwDocumentSearchDescription(PropertyDescriptorFactory propertyDescriptorFactory,
                                     FacetDescriptionFactory facetDescriptionFactory) {
    propertyParserFactory = new PropertyParserFactory();
    this.propertyDescriptorFactory = propertyDescriptorFactory;

    dataDescriptors = createDataDescriptors();
    facetDescriptions = createFacetDescriptions(facetDescriptionFactory);

    idDescriptor = propertyDescriptorFactory
            .getLocal(ID_DB_PROP, String.class);
    displayNameDescriptor = createDisplayNameDescriptor();
  }

  private List<FacetDescription> createFacetDescriptions(FacetDescriptionFactory facetDescriptionFactory) {
    return Lists.newArrayList(
            facetDescriptionFactory.createDatableRangeFacetDescription("dynamic_i_date", "wwdocument_date"),
            facetDescriptionFactory.createListFacetDescription(
                    "dynamic_s_origin", LocationNames.class, "names", "hasPublishLocation"),

            facetDescriptionFactory.createListFacetDescription(
                    "dynamic_s_document_type", DocumentType.class, "wwdocument_documentType"));
  }

  private Map<String, PropertyDescriptor> createDataDescriptors() {
    Map<String, PropertyDescriptor> dataDescriptors = Maps.newHashMap();
    dataDescriptors.put("_id", propertyDescriptorFactory.getLocal(ID_DB_PROP, String.class));
    dataDescriptors.put("authorName", createAuthorDescriptor());
    dataDescriptors.put("title", propertyDescriptorFactory.getLocal("wwdocument_title", String.class));
    dataDescriptors.put("date", propertyDescriptorFactory.getLocal("wwdocument_date", Datable.class));
    dataDescriptors.put("authorGender", propertyDescriptorFactory.getDerived(
            "isCreatedBy",
            "wwperson_gender",
            Gender.class));
    dataDescriptors.put("documentType", propertyDescriptorFactory
            .getLocal("wwdocument_documentType", DocumentType.class));
    dataDescriptors.put("modified_date", propertyDescriptorFactory
            .getLocal("modified", propertyParserFactory.getParser(Change.class)));
    dataDescriptors.put("genre", propertyDescriptorFactory
            .getDerived("hasGenre", "wwkeyword_value", String.class));
    dataDescriptors.put("publishLocation", propertyDescriptorFactory.getDerived(
            "hasPublishLocation",
            "names",
            LocationNames.class));
    dataDescriptors.put("language", propertyDescriptorFactory.getDerived(
            "hasWorkLanguage",
            "wwlanguage_name",
            String.class));

    return dataDescriptors;
  }

  @Override
  public List<String> getSortableFields() {
    return SORTABLE_FIELDS;
  }

  @Override
  public List<String> getFullTextSearchFields() {
    return FULL_TEXT_SEARCH_FIELDS;
  }

  private PropertyDescriptor createDisplayNameDescriptor() {
    PropertyDescriptor titleDescriptor = propertyDescriptorFactory.getLocal("wwdocument_title", String.class);
    PropertyDescriptor dateDescriptor = propertyDescriptorFactory.getLocal("wwdocument_date", Datable.class, "(", ")");

    PropertyDescriptor documentDescriptor = propertyDescriptorFactory.getAppender(titleDescriptor, dateDescriptor, " ");

    return propertyDescriptorFactory.getAppender(createAuthorDescriptor(), documentDescriptor, " - ");
  }

  private PropertyDescriptor createAuthorDescriptor() {
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

    return propertyDescriptorFactory
            .getComposite(authorNameDescriptor, authorTempNameDescriptor);
  }

  @Override
  protected List<FacetDescription> getFacetDescriptions() {
    return facetDescriptions;
  }

  @Override
  protected Map<String, PropertyDescriptor> getDataPropertyDescriptors() {
    return dataDescriptors;
  }

  @Override
  protected PropertyDescriptor getDisplayNameDescriptor() {
    return displayNameDescriptor;
  }

  @Override
  protected PropertyDescriptor getIdDescriptor() {
    return idDescriptor;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public List<FullTextSearchDescription> getFullTextSearchDescriptions() {
    return Lists.newArrayList();
  }

}
