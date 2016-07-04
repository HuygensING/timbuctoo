package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
    PROPERTY_TYPES.put("default-person-display-name", LocalProperty.class);
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

  public static ReadableProperty getForType(String clientName, String type, String[] options)
    throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    Class<? extends ReadableProperty> propertyClass = PROPERTY_TYPES.get(type);
    if (propertyClass == null) {
      throw new IllegalArgumentException("Property type does not exist: " + type);
    }
    if (propertyClass.isAssignableFrom(WwDocumentDisplayName.class)) {
      return new WwDocumentDisplayName();
    } else if (propertyClass.isAssignableFrom(WwPersonDisplayName.class)) {
      return new WwPersonDisplayName();
    }

    return new LocalProperty(clientName, Converters.forType(type, options));
  }

  public static ReadableProperty load(Vertex propertyVertex)
    throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException,
    InvocationTargetException {
    final String type = propertyVertex.value(ReadableProperty.PROPERTY_TYPE_NAME);
    final String clientName = propertyVertex.value(ReadableProperty.CLIENT_PROPERTY_NAME);

    String[] options = null;
    if (propertyVertex.property(LocalProperty.OPTIONS_PROPERTY_NAME).isPresent()) {
      final String optionsJson = propertyVertex.value(LocalProperty.OPTIONS_PROPERTY_NAME);
      options = new ObjectMapper().readValue(optionsJson, new TypeReference<String[]>() { });
    }

    return getForType(clientName, type, options);
  }
}
