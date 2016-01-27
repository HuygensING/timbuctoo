package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;

public class FacetDescriptionFactory {
  private final PropertyParserFactory parserFactory;

  public FacetDescriptionFactory(PropertyParserFactory parserFactory) {
    this.parserFactory = parserFactory;
  }

  public FacetDescription createListFacetDescription(String propertyName, PropertyParser parser, String facetName) {
    return new ListFacetDescription(facetName, propertyName, parser);
  }

  public FacetDescription createListFacetDescription(String facetName, Class<?> typeToParse, String propertyName) {
    return this.createListFacetDescription(propertyName, parserFactory.getParser(typeToParse), facetName);
  }

  public FacetDescription createListFacetDescription(String facetName, PropertyParser parser, String propertyName,
                                                     String relation) {
    return new DerivedListFacetDescription(facetName, propertyName, parser, relation);
  }

  public FacetDescription createListFacetDescription(String facetName, Class<?> typeToParse, String propertyName,
                                                     String relation) {
    return this.createListFacetDescription(facetName, parserFactory.getParser(typeToParse), propertyName, relation);
  }
}
