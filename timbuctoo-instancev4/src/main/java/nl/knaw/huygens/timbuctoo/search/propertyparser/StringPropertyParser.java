package nl.knaw.huygens.timbuctoo.search.propertyparser;

import nl.knaw.huygens.timbuctoo.search.PropertyParser;

class StringPropertyParser implements PropertyParser {
  @Override
  public String parse(String value) {
    return value;
  }
}
