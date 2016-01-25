package nl.knaw.huygens.timbuctoo.server.search.property;

import nl.knaw.huygens.timbuctoo.server.search.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;

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
}
