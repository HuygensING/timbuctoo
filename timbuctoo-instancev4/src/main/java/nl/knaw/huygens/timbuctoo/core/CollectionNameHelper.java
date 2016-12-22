package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;

import java.util.Objects;

public class CollectionNameHelper {
  public static final String RDF_URI_PREFIX = "https://repository.huygens.knaw.nl/";
  public static final String DEFAULT_COLLECTION_ENTITY_NAME = "unknown";
  private static final String ADMIN_VRE_NAME = "Admin";

  public static String defaultCollectionName(Vre vre) {
    return collectionName(DEFAULT_COLLECTION_ENTITY_NAME, vre);
  }

  public static String collectionName(String unprefixedEntityName, Vre vre) {

    return entityTypeName(unprefixedEntityName, vre) + "s";
  }

  public static String defaultEntityTypeName(Vre vre) {
    return entityTypeName(DEFAULT_COLLECTION_ENTITY_NAME, vre);
  }

  public static String entityTypeName(String unprefixedEntityName, Vre vre) {
    return Objects.equals(vre.getVreName(), ADMIN_VRE_NAME) ?
      unprefixedEntityName : vre.getVreName() + unprefixedEntityName;
  }

  public static String defaultRdfUri(Vre vre) {
    return rdfUri(DEFAULT_COLLECTION_ENTITY_NAME, vre);
  }

  public static String rdfUri(String unprefixedEntityName, Vre vre) {
    return RDF_URI_PREFIX + entityTypeName(unprefixedEntityName, vre);
  }
}
