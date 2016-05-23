package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;

public class StringListParser implements PropertyParser {
  private String separator;

  public StringListParser(String seperator) {
    this.separator = seperator;
  }

  public StringListParser() {
    this(";");
  }

  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }
    return value.replace("[", "").replace("]", "").replace("\" ", "").replace(" \"", "").replace("\"", "")
                .replace(",", separator);
  }

  @Override
  public Comparable<?> parseForSort(String value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
