package nl.knaw.huygens.timbuctoo.model.properties;

import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;

import java.util.HashMap;
import java.util.Map;

public class PropertyTypes {
  private static final Map<String, Class<? extends ReadableProperty>> PROPERTY_TYPES = new HashMap<>();

  static {
    PROPERTY_TYPES.put("person-names", LocalProperty.class);
    PROPERTY_TYPES.put("encoded-array-of-limited-values", LocalProperty.class);
    PROPERTY_TYPES.put("encoded-string-of-limited-values", LocalProperty.class);
    PROPERTY_TYPES.put("datable", LocalProperty.class);
    PROPERTY_TYPES.put("string", LocalProperty.class);
    PROPERTY_TYPES.put("hyperlinks", LocalProperty.class);
    PROPERTY_TYPES.put("unencoded-string-of-limited-values", LocalProperty.class);
    PROPERTY_TYPES.put("encoded-array", LocalProperty.class);
    PROPERTY_TYPES.put("altnames", LocalProperty.class);
    PROPERTY_TYPES.put("default-person-display-name", ReadableProperty.class);
    PROPERTY_TYPES.put("wwperson-display-name", WwPersonDisplayName.class);
    PROPERTY_TYPES.put("wwdocument-display-name", WwDocumentDisplayName.class);
  }

  public static LocalProperty localProperty(String propName) {
    return localProperty(propName, Converters.stringToString);
  }

  public static LocalProperty localProperty(String propName, Converter converter) {
    return new LocalProperty(propName, converter);
  }

  public static ReadableProperty wwPersonNameOrTempName() {
    return new WwPersonDisplayName();
  }

  public static ReadableProperty wwdocumentDisplayNameProperty() {
    return new WwDocumentDisplayName();
  }
}
