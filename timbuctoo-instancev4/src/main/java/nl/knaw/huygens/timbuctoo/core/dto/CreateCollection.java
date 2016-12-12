package nl.knaw.huygens.timbuctoo.core.dto;

import nl.knaw.huygens.timbuctoo.database.CollectionNameHelper;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;

import java.util.Objects;

public class CreateCollection {
  private final String unprefixedEntityName;

  public CreateCollection(String unprefixedEntityName) {
    this.unprefixedEntityName = unprefixedEntityName;
  }

  public static CreateCollection forEntityTypeName(String unprefixedEntityTypeName) {
    return new CreateCollection(unprefixedEntityTypeName);
  }

  public String getEntityTypeName(Vre vre) {
    return CollectionNameHelper.entityTypeName(unprefixedEntityName, vre);
  }

  public String getCollectionName(Vre vre) {
    return CollectionNameHelper.collectionName(unprefixedEntityName, vre);
  }

  public boolean isUknownCollection() {
    return Objects.equals(unprefixedEntityName, "unknonwn");
  }

  public String getRdfUri(Vre vre) {
    return CollectionNameHelper.rdfUri(unprefixedEntityName, vre);
  }
}
