package nl.knaw.huygens.timbuctoo.search.description;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.Gender;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchDescriptionFactory {
  private FacetDescriptionFactory facetDescriptionFactory;
  private PropertyDescriptorFactory propertyDescriptorFactory;

  public SearchDescriptionFactory() {
    PropertyParserFactory propertyParserFactory = new PropertyParserFactory();
    facetDescriptionFactory = new FacetDescriptionFactory(propertyParserFactory);
    propertyDescriptorFactory = new PropertyDescriptorFactory(propertyParserFactory);
  }

  SearchDescriptionFactory(FacetDescriptionFactory facetDescriptionFactory,
                           PropertyDescriptorFactory propertyDescriptorFactory) {
    this.facetDescriptionFactory = facetDescriptionFactory;
    this.propertyDescriptorFactory = propertyDescriptorFactory;
  }

  public SearchDescription create(String entityName) {
    if (Objects.equals(entityName, "wwperson")) {
      return getWwPersonSearchDescription();
    } else if (Objects.equals(entityName, "wwdocument")) {
      return new WwDocumentSearchDescription();
    }
    throw new IllegalArgumentException(String.format("No SearchDescription for '%s'", entityName));
  }

  private SearchDescription getWwPersonSearchDescription() {
    Map<String, PropertyDescriptor> dataPropertyDescriptors = Maps.newHashMap();
    dataPropertyDescriptors.put("birthDate", propertyDescriptorFactory.getLocal("wwperson_birthDate", Datable.class));
    dataPropertyDescriptors.put("deathDate", propertyDescriptorFactory.getLocal("wwperson_deathDate", Datable.class));
    dataPropertyDescriptors.put("gender", propertyDescriptorFactory.getLocal("wwperson_gender", Gender.class));
    dataPropertyDescriptors.put("modified_date", propertyDescriptorFactory.getLocal("modified", Change.class));
    dataPropertyDescriptors.put("residenceLocation", propertyDescriptorFactory.getDerived(
      "hasResidenceLocation",
      "names",
      LocationNames.class));
    dataPropertyDescriptors.put("name", propertyDescriptorFactory.getComposite(
      propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
      propertyDescriptorFactory.getLocal("wwperson_tempName", String.class)));
    dataPropertyDescriptors
      .put("_id", propertyDescriptorFactory.getLocal("tim_id", String.class));

    List<FacetDescription> facetDescriptions = Lists.newArrayList(
      facetDescriptionFactory.createListFacetDescription("dynamic_s_gender", Gender.class, "wwperson_gender"),
      facetDescriptionFactory
        .createListFacetDescription("dynamic_s_deathplace", LocationNames.class, "names", "hasDeathPlace"),
      facetDescriptionFactory
        .createListFacetDescription("dynamic_s_birthplace", LocationNames.class, "names", "hasBirthPlace"),
      facetDescriptionFactory.createListFacetDescription(
        "dynamic_s_relatedLocations",
        LocationNames.class,
        "names",
        "hasBirthPlace", "hasDeathPlace", "hasResidenceLocation"),
      facetDescriptionFactory.createRangeFacetDescription("dynamic_i_deathDate", "wwperson_deathDate"),
      facetDescriptionFactory.createListFacetDescription("dynamic_s_children", String.class, "wwperson_children"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_religion", "hasReligion"),
      facetDescriptionFactory.createListFacetDescription("dynamic_s_residence", LocationNames.class, "wwlocation_names",
        "hasResidenceLocation"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_marital_status", "hasMaritalStatus"),
      facetDescriptionFactory
        .createListFacetDescription("dynamic_s_collective", String.class, "wwcollective_name", "isMemberOf"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_education", "hasEducation"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_social_class", "hasSocialClass"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_financials", "hasFinancialStatus"),
      facetDescriptionFactory.createRangeFacetDescription("dynamic_i_birthDate", "wwperson_birthDate"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_profession", "hasProfession"));

    PropertyDescriptor displayNameDescriptor = propertyDescriptorFactory.getComposite(
      propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
      propertyDescriptorFactory.getLocal("wwperson_tempName", String.class));
    PropertyDescriptor idDescriptor = propertyDescriptorFactory
      .getLocal(SearchDescription.ID_DB_PROP, new PropertyParserFactory().getParser(String.class));

    List<String> sortableFields = Lists.newArrayList(
      "dynamic_k_modified",
      "dynamic_k_birthDate",
      "dynamic_sort_name",
      "dynamic_k_deathDate");
    List<String> fullTextSearchFields = Lists.newArrayList(
      "dynamic_t_tempspouse",
      "dynamic_t_notes",
      "dynamic_t_name");
    String type = "wwperson";
    return new DefaultSearchDescription(
      idDescriptor,
      displayNameDescriptor,
      facetDescriptions,
      dataPropertyDescriptors,
      sortableFields,
      fullTextSearchFields,
      type);
  }

}
