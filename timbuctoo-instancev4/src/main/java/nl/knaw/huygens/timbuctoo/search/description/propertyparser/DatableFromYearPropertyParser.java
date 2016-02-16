package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.commons.lang3.StringUtils;

class DatableFromYearPropertyParser implements PropertyParser {

  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }

    String cleanedValue = StringUtils.strip(value, "\"");
    Datable datable = new Datable(cleanedValue);

    return datable.isValid() ? String.valueOf(datable.getFromYear()) : null;
  }
}
