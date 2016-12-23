package nl.knaw.huygens.timbuctoo.database;

public class PropertyNameHelper {
  public static String createPropName(String entityTypeName, String rdfUri) {
    return entityTypeName + "_" + rdfUri;
  }
}
