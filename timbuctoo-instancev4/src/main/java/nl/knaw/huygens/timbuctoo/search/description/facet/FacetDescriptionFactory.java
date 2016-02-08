package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;

public class FacetDescriptionFactory {
  private final PropertyParserFactory parserFactory;

  public FacetDescriptionFactory(PropertyParserFactory parserFactory) {
    this.parserFactory = parserFactory;
  }

  public FacetDescription createListFacetDescription(String facetName, PropertyParser parser, String propertyName) {
    return new ListFacetDescription(facetName, propertyName, parser);
  }

  public FacetDescription createListFacetDescription(String facetName, Class<?> typeToParse, String propertyName) {
    return this.createListFacetDescription(facetName, parserFactory.getParser(typeToParse), propertyName);
  }

  public FacetDescription createListFacetDescription(String facetName, PropertyParser parser, String propertyName,
                                                     String... relations) {
    return new RelatedListFacetDescription(facetName, propertyName, parser, relations);
  }

  public FacetDescription createListFacetDescription(String facetName, Class<?> typeToParse, String propertyName,
                                                     String... relations) {
    return this.createListFacetDescription(facetName, parserFactory.getParser(typeToParse), propertyName, relations);
  }

  /**
   * A convenience method, for creating a {@code ListFacetDescription} for related keywords.
   */
  public FacetDescription createKeywordDescription(String facetName, String relationName, final String projectPrefix) {
    String propertyName = String.format("%skeyword_value", projectPrefix);
    return this.createListFacetDescription(facetName, String.class, propertyName, relationName);
  }

  public FacetDescription createDatableRangeFacetDescription(String facetName, String propertyName) {
    return new DatableRangeFacetDescription(facetName, propertyName);
  }

  public FacetDescription createChangeRangeFacetDescription(String facetName, String propertyName) {
    return new ChangeRangeFacetDescription(facetName, propertyName);
  }

  public FacetDescription createWwPersonLanguageFacetDescription(String facetName) {
    return new WwPersonLanguageFacetDescription(facetName);
  }


}
