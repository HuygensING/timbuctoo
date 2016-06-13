package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.DocumentType;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.search.description.sort.SortDescription;
import nl.knaw.huygens.timbuctoo.search.description.sort.SortFieldDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.knaw.huygens.timbuctoo.search.description.Property.localProperty;
import static nl.knaw.huygens.timbuctoo.search.description.sort.BuildableSortFieldDescription.newSortFieldDescription;

public class CharterDocumentSearchDescription extends AbstractSearchDescription {

  private final List<String> sortableFields;
  private final List<String> fullTextSearchFields;
  private final PropertyDescriptor displayNameDescriptor;
  private final PropertyDescriptor idDescriptor;
  private final List<FacetDescription> facetDescriptions;
  private final Map<String, PropertyDescriptor> dataPropertyDescriptors;
  private final List<FullTextSearchDescription> fullTextSearchDescriptions;
  private final PropertyParserFactory propertyParserFactory;

  private List<SortFieldDescription> sortFieldDescriptions;
  
  private static final Logger LOG = LoggerFactory.getLogger(CharterDocumentSearchDescription.class);
  
  public CharterDocumentSearchDescription(PropertyDescriptorFactory propertyDescriptorFactory,
                                   FacetDescriptionFactory facetDescriptionFactory) {
    propertyParserFactory = new PropertyParserFactory();
    sortableFields = Lists.newArrayList(
      "dynamic_sort_title",
      "dynamic_k_date",
      "dynamic_sort_creator"
      );
    fullTextSearchFields = Lists.newArrayList();

    displayNameDescriptor = propertyDescriptorFactory.getLocal("charterdocument_inventaristekst",
        new PropertyParserFactory().getJoinedListParser(" ")); 
    idDescriptor = propertyDescriptorFactory
      .getLocal(SearchDescription.ID_DB_PROP, String.class);

    facetDescriptions = createFacetDescriptions(facetDescriptionFactory);
    dataPropertyDescriptors = createDataPropertyDescriptions(propertyDescriptorFactory);
    fullTextSearchDescriptions = createFullTextSearchDescriptions();

    sortFieldDescriptions = createSortFieldDescriptions();
  }

  protected ArrayList<SortFieldDescription> createSortFieldDescriptions() {
    PropertyParserFactory ppf = new PropertyParserFactory();
    return Lists.newArrayList(
      newSortFieldDescription()
        .withName("dynamic_k_date")
        .withDefaultValue(0)
        .withProperty(localProperty()
          .withName("charterdocument_date"))
        .build(),
        newSortFieldDescription()
        .withName("dynamic_sort_title")
        .withDefaultValue("")
        .withProperty(localProperty()
          .withName("charterdocument_title"))
        .build());
  }

  private ArrayList<FullTextSearchDescription> createFullTextSearchDescriptions() {
    return Lists.newArrayList();
  }

  private List<FacetDescription> createFacetDescriptions(FacetDescriptionFactory facetDescriptionFactory) {
    return Lists.newArrayList(
      facetDescriptionFactory.createListFacetDescription("dynamic_s_archief", String.class, "charterdocument_archief"),
      facetDescriptionFactory.createCharterFondsFacetDescription("dynamic_s_fonds"),
      facetDescriptionFactory.createListFacetDescription("dynamic_s_editions", String.class,
          "charterdocument_descriptionOfEditions"),
      facetDescriptionFactory.createDatableRangeFacetDescription("dynamic_i_date", "charterdocument_date")
      );
  }

  private Map<String, PropertyDescriptor> createDataPropertyDescriptions(
    PropertyDescriptorFactory propertyDescriptorFactory) {
    Map<String, PropertyDescriptor> dataPropertyDescriptors = Maps.newHashMap();
    dataPropertyDescriptors.put("date", propertyDescriptorFactory.getLocal("charterdocument_date", Datable.class));
    dataPropertyDescriptors.put("_id", propertyDescriptorFactory.getLocal(ID_DB_PROP, String.class));
    dataPropertyDescriptors.put("title", propertyDescriptorFactory.getLocal("charterdocument_title", String.class));
    dataPropertyDescriptors.put("archief", propertyDescriptorFactory.getLocal("charterdocument_archief", String.class));
    dataPropertyDescriptors.put("fonds", propertyDescriptorFactory.getLocal("charterdocument_fonds", String.class));
    dataPropertyDescriptors.put("fondsNaam", propertyDescriptorFactory
        .getLocal("charterdocument_fondsNaam", String.class));
    dataPropertyDescriptors.put("inventarisNummer", propertyDescriptorFactory
        .getLocal("charterdocument_inventarisNummer", String.class));
    dataPropertyDescriptors.put("inventaristekst", propertyDescriptorFactory
        .getLocal("charterdocument_inventaristekst", new PropertyParserFactory().getJoinedListParser(" ")));
    dataPropertyDescriptors.put("volgNummer", propertyDescriptorFactory
        .getLocal("charterdocument_volgNummer", String.class));
    dataPropertyDescriptors.put("regestNummer", propertyDescriptorFactory
        .getLocal("charterdocument_regestNummer", String.class));
    dataPropertyDescriptors.put("tekstRegest", propertyDescriptorFactory
        .getLocal("charterdocument_tekstRegest", new PropertyParserFactory().getJoinedListParser("; ")));
    dataPropertyDescriptors.put("descriptionOfEditions", propertyDescriptorFactory
        .getLocal("charterdocument_descriptionOfEditions", String.class));
    dataPropertyDescriptors.put("overige", propertyDescriptorFactory
        .getLocal("charterdocument_overige", String.class));
    dataPropertyDescriptors.put("resourceType", propertyDescriptorFactory
        .getLocal("charterdocument_resourceType", String.class));
    dataPropertyDescriptors.put("thumbs", propertyDescriptorFactory
        .getLocal("charterdocument_thumbs", String.class));
    dataPropertyDescriptors.put("links", propertyDescriptorFactory
        .getLocal("charterdocument_links", String.class));
    dataPropertyDescriptors.put("documentType", propertyDescriptorFactory
        .getLocal("charterdocument_documentType", DocumentType.class));
    dataPropertyDescriptors.put("tekstRegest", propertyDescriptorFactory
        .getLocal("charterdocument_tekstRegest", String.class));
    dataPropertyDescriptors.put("additioneleInformatie", propertyDescriptorFactory
        .getLocal("charterdocument_additioneleInformatie", String.class));
    dataPropertyDescriptors.put("transcription", propertyDescriptorFactory
        .getLocal("charterdocument_transcription", String.class));
    dataPropertyDescriptors.put("translation", propertyDescriptorFactory
        .getLocal("charterdocument_translation", String.class));
    dataPropertyDescriptors.put("namesInCharter", propertyDescriptorFactory
        .getLocal("charterdocument_namesInCharter", String.class));
    dataPropertyDescriptors.put("modified_date", propertyDescriptorFactory
        .getLocal("modified", propertyParserFactory.getParser(Change.class)));
    return dataPropertyDescriptors;
  }

  @Override
  protected List<FacetDescription> getFacetDescriptions() {
    return facetDescriptions;
  }

  @Override
  protected Map<String, PropertyDescriptor> getDataPropertyDescriptors() {
    return dataPropertyDescriptors;
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
    return "charterdocument";
  }

  @Override
  public List<FullTextSearchDescription> getFullTextSearchDescriptions() {
    return fullTextSearchDescriptions;
  }

  @Override
  public List<String> getSortableFields() {
    return sortableFields;
  }

  @Override
  public List<String> getFullTextSearchFields() {
    return fullTextSearchFields;
  }

  @Override
  protected SortDescription getSortDescription() {

    return new SortDescription(sortFieldDescriptions);
  }
}
