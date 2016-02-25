package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.commons.lang.StringUtils;

public class DocumentTypePropertyParser implements PropertyParser {
  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }

    return StringUtils.strip(value, "\"");
  }

  @Override
  public Comparable<?> parseToRaw(String value) {
    return value == null ? getDefaultValue() : parse(value);
  }

  @Override
  public Comparable<?> getDefaultValue() {
    return "";
  }
}

