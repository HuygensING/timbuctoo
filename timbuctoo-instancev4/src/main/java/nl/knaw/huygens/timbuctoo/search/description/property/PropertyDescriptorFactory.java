package nl.knaw.huygens.timbuctoo.search.description.property;

import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;

public class PropertyDescriptorFactory {
  public PropertyDescriptor getLocal(String propertyName, PropertyParser parser) {
    return new LocalPropertyDescriptor(propertyName, parser);
  }

  public PropertyDescriptor getComposite(PropertyDescriptor preferred, PropertyDescriptor backUp) {
    return new CompositePropertyDescriptor(preferred, backUp);
  }

  public PropertyDescriptor getDerived(String relationName, String propertyName, PropertyParser parser) {
    return new DerivedPropertyDescriptor(relationName, propertyName, parser);
  }

  public PropertyDescriptor getDerivedWithSeparator(String relationName, String propertyName, PropertyParser parser,
                                                    String separator) {
    return new DerivedPropertyDescriptor(relationName, propertyName, parser, separator);
  }
}
