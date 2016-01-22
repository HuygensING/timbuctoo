package nl.knaw.huygens.timbuctoo.server.search.propertyparser;

import nl.knaw.huygens.timbuctoo.server.search.Datable;
import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;

class DatableFromYearPropertyParser implements PropertyParser {

  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }

    Datable datable = new Datable(value);

    return datable.isValid() ? String.valueOf(datable.getFromYear()) : null;
  }
}
