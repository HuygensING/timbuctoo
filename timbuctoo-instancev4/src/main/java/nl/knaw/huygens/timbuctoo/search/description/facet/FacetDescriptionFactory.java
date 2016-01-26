package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;

public class FacetDescriptionFactory {
  public FacetDescription createListFacetDescription(String facetName, String propertyName, PropertyParser parser) {
    return new ListFacetDescription(facetName, propertyName, parser);
  }
}
