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

import java.util.List;
import java.util.Map;

public class WwPersonSearchDescription extends AbstractSearchDescription {


  private final List<String> sortableFields;
  private final List<String> fullTextSearchFields;
  private final PropertyDescriptor displayNameDescriptor;
  private final PropertyDescriptor idDescriptor;
  private final List<FacetDescription> facetDescriptions;
  private final Map<String, PropertyDescriptor> dataPropertyDescriptors;

  public WwPersonSearchDescription(PropertyDescriptorFactory propertyDescriptorFactory,
                                   FacetDescriptionFactory facetDescriptionFactory) {
    sortableFields = Lists.newArrayList(
      "dynamic_k_modified",
      "dynamic_k_birthDate",
      "dynamic_sort_name",
      "dynamic_k_deathDate");
    fullTextSearchFields = Lists.newArrayList(
      "dynamic_t_tempspouse",
      "dynamic_t_notes",
      "dynamic_t_name");

    displayNameDescriptor = propertyDescriptorFactory.getComposite(
      propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
      propertyDescriptorFactory.getLocal("wwperson_tempName", String.class));
    idDescriptor = propertyDescriptorFactory
      .getLocal(SearchDescription.ID_DB_PROP, String.class);

    facetDescriptions = createFacetDescriptions(facetDescriptionFactory);
    dataPropertyDescriptors = createDataPropertyDescriptions(propertyDescriptorFactory);
  }

  private List<FacetDescription> createFacetDescriptions(FacetDescriptionFactory facetDescriptionFactory) {
    return Lists.newArrayList(
      facetDescriptionFactory.createListFacetDescription("dynamic_s_gender", Gender.class, "wwperson_gender"),
      facetDescriptionFactory
        .createListFacetDescription("dynamic_s_deathplace", LocationNames.class, "wwlocation_names", "hasDeathPlace"),
      facetDescriptionFactory
        .createListFacetDescription("dynamic_s_birthplace", LocationNames.class, "wwlocation_names", "hasBirthPlace"),
      facetDescriptionFactory.createListFacetDescription(
        "dynamic_s_relatedLocations",
        LocationNames.class,
        "wwlocation_names",
        "hasBirthPlace", "hasDeathPlace", "hasResidenceLocation"),
      facetDescriptionFactory.createDatableRangeFacetDescription("dynamic_i_deathDate", "wwperson_deathDate"),
      facetDescriptionFactory.createListFacetDescription("dynamic_s_children", String.class, "wwperson_children"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_religion", "hasReligion", "ww"),
      facetDescriptionFactory.createListFacetDescription("dynamic_s_residence", LocationNames.class, "wwlocation_names",
        "hasResidenceLocation"),
      facetDescriptionFactory.createWwPersonLanguageFacetDescription("dynamic_s_language"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_marital_status", "hasMaritalStatus", "ww"),
      facetDescriptionFactory
        .createListFacetDescription("dynamic_s_collective", String.class, "wwcollective_name", "isMemberOf"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_education", "hasEducation", "ww"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_social_class", "hasSocialClass", "ww"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_financials", "hasFinancialStatus", "ww"),
      facetDescriptionFactory.createDatableRangeFacetDescription("dynamic_i_birthDate", "wwperson_birthDate"),
      facetDescriptionFactory.createKeywordDescription("dynamic_s_profession", "hasProfession", "ww"),
      facetDescriptionFactory.createChangeRangeFacetDescription("dynamic_i_modified", "modified"));
  }

  private Map<String, PropertyDescriptor> createDataPropertyDescriptions(
    PropertyDescriptorFactory propertyDescriptorFactory) {
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
  protected String getType() {
    return "wwperson";
  }

  @Override
  public List<String> getSortableFields() {
    return sortableFields;
  }

  @Override
  public List<String> getFullTextSearchFields() {
    return fullTextSearchFields;
  }
}
