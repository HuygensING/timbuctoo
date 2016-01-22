package nl.knaw.huygens.timbuctoo.server.search.propertyparser;

import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;
import org.apache.commons.lang.StringUtils;

class GenderPropertyParser implements PropertyParser {
  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }

    return StringUtils.strip(value, "\"");
  }
}
