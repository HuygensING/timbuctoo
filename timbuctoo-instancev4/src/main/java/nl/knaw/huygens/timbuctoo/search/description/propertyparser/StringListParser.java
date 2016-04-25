package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;

public class StringListParser implements PropertyParser {
  @Override
  public String parse(String value) {
    if (value == null) {
      return null;
    }
    return value.replace("[", "").replace("]", "").replace("\"", "").replace(" ", "").replace(',', ';');
  }

  @Override
  public Comparable<?> parseForSort(String value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
