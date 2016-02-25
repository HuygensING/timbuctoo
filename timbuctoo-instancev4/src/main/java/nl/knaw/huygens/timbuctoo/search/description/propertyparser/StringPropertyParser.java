package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;

class StringPropertyParser implements PropertyParser {
  @Override
  public String parse(String value) {
    return value;
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
