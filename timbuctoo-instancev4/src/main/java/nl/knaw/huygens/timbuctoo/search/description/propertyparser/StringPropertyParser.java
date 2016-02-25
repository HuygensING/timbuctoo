package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;

class StringPropertyParser implements PropertyParser {
  @Override
  public String parse(String value) {
    return value;
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
