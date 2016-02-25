package nl.knaw.huygens.timbuctoo.search.description;

public interface PropertyParser {
  String parse(String value);

  Object parseToRaw(String value);

  Object getDefaultValue();
}
