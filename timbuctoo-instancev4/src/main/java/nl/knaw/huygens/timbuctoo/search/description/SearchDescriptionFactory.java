package nl.knaw.huygens.timbuctoo.search.description;


import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;

import java.util.Objects;
import java.util.Optional;

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

  public Optional<SearchDescription> create(String entityName) {
    if (Objects.equals(entityName, "wwperson")) {
      return Optional.of(new WwPersonSearchDescription(propertyDescriptorFactory, facetDescriptionFactory));
    } else if (Objects.equals(entityName, "wwdocument")) {
      return Optional.of(new WwDocumentSearchDescription());
    } else if (Objects.equals(entityName, "wwcollective")) {
      return Optional.of(new WwCollectiveSearchDescription());
    }
    return Optional.empty();
  }

}
