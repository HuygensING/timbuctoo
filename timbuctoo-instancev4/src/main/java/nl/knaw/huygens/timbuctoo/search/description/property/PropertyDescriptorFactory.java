package nl.knaw.huygens.timbuctoo.search.description.property;

import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;

public class PropertyDescriptorFactory {
  private final PropertyParserFactory parserFactory;

  public PropertyDescriptorFactory(PropertyParserFactory parserFactory) {
    this.parserFactory = parserFactory;
  }

  public PropertyDescriptor getLocal(String propertyName, PropertyParser parser) {
    return new LocalPropertyDescriptor(propertyName, parser);
  }

  public PropertyDescriptor getLocal(String propertyName, Class<?> typeToParse) {
    return this.getLocal(propertyName, parserFactory.getParser(typeToParse));
  }

  public PropertyDescriptor getComposite(PropertyDescriptor preferred, PropertyDescriptor backUp) {
    return new CompositePropertyDescriptor(preferred, backUp);
  }

  public PropertyDescriptor getDerived(String relationName, String propertyName, PropertyParser parser) {
    return new RelatedPropertyDescriptor(relationName, propertyName, parser);
  }

  public PropertyDescriptor getDerived(String relationName, String propertyName, Class<?> typeToParse) {
    return this.getDerived(relationName, propertyName, parserFactory.getParser(typeToParse));

  }

  public PropertyDescriptor getDerivedWithSeparator(String relationName, String propertyName, PropertyParser parser,
                                                    String separator) {
    return new RelatedPropertyDescriptor(relationName, propertyName, parser, separator);
  }
}
