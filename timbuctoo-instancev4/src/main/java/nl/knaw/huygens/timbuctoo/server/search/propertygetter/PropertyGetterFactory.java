package nl.knaw.huygens.timbuctoo.server.search.propertygetter;

import nl.knaw.huygens.timbuctoo.server.search.PropertyGetter;

public class PropertyGetterFactory {
  public PropertyGetter getLocal(String fieldName) {
    return new LocalPropertyGetter(fieldName);
  }
}
