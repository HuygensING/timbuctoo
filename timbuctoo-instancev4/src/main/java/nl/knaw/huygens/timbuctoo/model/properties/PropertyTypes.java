package nl.knaw.huygens.timbuctoo.model.properties;

import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;

import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.defaultFullPersonNameConverter;

public class PropertyTypes {

  public static ReadWriteProperty localProperty(String propName) {
    return localProperty(propName, Converters.stringToString);
  }

  public static ReadWriteProperty localProperty(String propName, Converter converter) {
    return new LocalProperty(propName, converter);
  }

  public static ReadOnlyProperty wwPersonNameOrTempName() {
    return new PropertyOrDefault(
      localProperty("wwperson_names", defaultFullPersonNameConverter),
      localProperty("wwperson_tempName")
    );
  }

  public static ReadOnlyProperty wwdocumentDisplayNameProperty() {
    return new WwDocumentDisplayName();
  }
}
