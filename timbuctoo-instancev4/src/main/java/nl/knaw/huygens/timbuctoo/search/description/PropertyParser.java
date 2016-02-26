package nl.knaw.huygens.timbuctoo.search.description;

public interface PropertyParser {
  String parse(String value);

  Comparable<?> parseToRaw(String value);
}
