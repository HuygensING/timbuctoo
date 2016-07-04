package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class PropertyTypes {

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

  public static ReadableProperty getForType(String dbName, String type, String[] options)
    throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {


    return new LocalProperty(dbName, Converters.forType(type, options));
  }

  public static ReadableProperty load(Vertex propertyVertex)
    throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException,
    InvocationTargetException {
    final String type = propertyVertex.value(ReadableProperty.PROPERTY_TYPE_NAME);

    if (type.equals(WwDocumentDisplayName.TYPE)) {
      return new WwDocumentDisplayName();
    } else if (type.equals(WwPersonDisplayName.TYPE)) {
      return new WwPersonDisplayName();
    }

    String[] options = null;
    if (propertyVertex.property(LocalProperty.OPTIONS_PROPERTY_NAME).isPresent()) {
      final String optionsJson = propertyVertex.value(LocalProperty.OPTIONS_PROPERTY_NAME);
      options = new ObjectMapper().readValue(optionsJson, new TypeReference<String[]>() { });
    }

    final String dbName = propertyVertex.value(LocalProperty.DATABASE_PROPERTY_NAME);
    return getForType(dbName, type, options);
  }
}
