package nl.knaw.huygens.timbuctoo.database;

public class PropertyNameHelper {
  public static String createPropName(String entityTypeName, String rdfUri) {
    if (rdfUri.startsWith("http://timbuctoo.huygens.knaw.nl/")) {
      return entityTypeName + "_" + rdfUri.substring("http://timbuctoo.huygens.knaw.nl/".length());
    } else {
      return entityTypeName + "_" + rdfUri;
    }
  }
}
