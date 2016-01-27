package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;

public class FacetDescriptionFactory {
  private final PropertyParserFactory parserFactory;

  public FacetDescriptionFactory(PropertyParserFactory parserFactory) {
    this.parserFactory = parserFactory;
  }

  //TODO: change order of arguments

  public FacetDescription createListFacetDescription(String facetName, String propertyName, PropertyParser parser) {
    return new ListFacetDescription(facetName, propertyName, parser);
  }

  public FacetDescription createListFacetDescription(String facetName, String propertyName, Class<?> typeToParse) {
    return this.createListFacetDescription(facetName, propertyName, parserFactory.getParser(typeToParse));
  }

  public FacetDescription createListFacetDescription(String facetName, String propertyName, PropertyParser parser,
                                                     String relation) {
    return new DerivedListFacetDescription(facetName, propertyName, parser, relation);
  }

  public FacetDescription createListFacetDescription(String facetName, String propertyName, Class<?> typeToParse,
                                                     String relation) {
    return this.createListFacetDescription(facetName, propertyName, parserFactory.getParser(typeToParse), relation);
  }
}
