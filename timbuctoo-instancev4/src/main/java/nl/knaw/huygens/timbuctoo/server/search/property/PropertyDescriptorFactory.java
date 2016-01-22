package nl.knaw.huygens.timbuctoo.server.search.property;

import nl.knaw.huygens.timbuctoo.server.search.PropDescriptor;
import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;

public class PropertyDescriptorFactory {
  public PropDescriptor getLocal(String propertyName, PropertyParser parser) {
    return new LocalPropertyDescriptor(propertyName, parser);
  }
}
