package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;

import java.util.Objects;

public class CollectionNameHelper {
  public static final String RDF_URI_PREFIX = "https://repository.huygens.knaw.nl/";
  public static final String DEFAULT_COLLECTION_ENTITY_NAME = "unknown";
  private static final String ADMIN_VRE_NAME = "Admin";

  public static String defaultCollectionName(Vre vre) {
    return defaultCollectionName(vre.getVreName());
  }

  public static String defaultCollectionName(String vreName) {
    return defaultEntityTypeName(vreName) + "s";
  }

  public static String collectionName(String unprefixedEntityName, Vre vre) {
    return collectionName(unprefixedEntityName, vre.getVreName());
  }

  public static String collectionName(String unprefixedEntityName, String vreName) {
    return entityTypeName(unprefixedEntityName, vreName) + "s";
  }

  public static String defaultEntityTypeName(Vre vre) {
    return defaultEntityTypeName(vre.getVreName());
  }

  public static String defaultEntityTypeName(String vrename) {
    return entityTypeName(DEFAULT_COLLECTION_ENTITY_NAME, vrename);
  }

  public static String entityTypeName(String unprefixedEntityName, Vre vre) {
    return entityTypeName(unprefixedEntityName, vre.getVreName());
  }

  public static String entityTypeName(String unprefixedEntityName, String vreName) {
    return Objects.equals(vreName, ADMIN_VRE_NAME) ?
      unprefixedEntityName : vreName + unprefixedEntityName;
  }

  public static String defaultRdfUri(Vre vre) {
    return rdfUri(DEFAULT_COLLECTION_ENTITY_NAME, vre);
  }

  public static String rdfUri(String unprefixedEntityName, Vre vre) {
    return RDF_URI_PREFIX + entityTypeName(unprefixedEntityName, vre);
  }
}
