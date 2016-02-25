package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.commons.lang.StringUtils;

class GenderPropertyParser implements PropertyParser {
  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }

    return StringUtils.strip(value, "\"");
  }

  @Override
  public Object parseToRaw(String value) {
    return value == null ? getDefaultValue() : parse(value);
  }

  @Override
  public Object getDefaultValue() {
    return "";
  }
}
