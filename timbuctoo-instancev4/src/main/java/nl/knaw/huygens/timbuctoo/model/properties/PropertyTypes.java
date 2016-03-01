package nl.knaw.huygens.timbuctoo.model.properties;

import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;

import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.defaultFullPersonNameConverter;

public class PropertyTypes {

  public static TimbuctooProperty localProperty(String propName) {
    return localProperty(propName, Converters.stringToString);
  }

  public static TimbuctooProperty localProperty(String propName, Converter converter) {
    return new LocalProperty(propName, converter);
  }

  public static TimbuctooProperty wwPersonNameOrTempName() {
    return new PropertyOrDefault(
      localProperty("wwperson_names", defaultFullPersonNameConverter),
      localProperty("wwperson_tempName"),
      "Displayname cannot be set, set the 'names' property instead."
    );
  }

  public static TimbuctooProperty wwdocumentDisplayNameProperty() {
    return new WwDocumentDisplayName();
  }
}
