package nl.knaw.huygens.timbuctoo.rml;

public interface Row {
  Object get(String key);

  void handleLinkError(String childField, String parentCollection, String parentField);
}
