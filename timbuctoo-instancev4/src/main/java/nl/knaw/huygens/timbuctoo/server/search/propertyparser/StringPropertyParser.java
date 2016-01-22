package nl.knaw.huygens.timbuctoo.server.search.propertyparser;

import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;

class StringPropertyParser implements PropertyParser {
  @Override
  public String parse(String value) {
    return value;
  }
}
