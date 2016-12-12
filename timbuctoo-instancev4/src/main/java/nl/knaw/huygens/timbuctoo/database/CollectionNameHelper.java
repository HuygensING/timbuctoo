package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;

import java.util.Objects;

public class CollectionNameHelper {
  static final String RDF_URI_PREFIX = "https://repository.huygens.knaw.nl/";
  private static final String ADMIN_VRE_NAME = "Admin";

  public static String collectionName(String unprefixedEntityName, Vre vre) {

    return entityTypeName(unprefixedEntityName, vre) + "s";
  }

  public static String entityTypeName(String unprefixedEntityName, Vre vre) {
    return Objects.equals(vre.getVreName(), ADMIN_VRE_NAME) ?
      unprefixedEntityName : vre.getVreName() + unprefixedEntityName;
  }

  public static String rdfUri(String unprefixedEntityName, Vre vre) {
    return RDF_URI_PREFIX + entityTypeName(unprefixedEntityName, vre);
  }
}
