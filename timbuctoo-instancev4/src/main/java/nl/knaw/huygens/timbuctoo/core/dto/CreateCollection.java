package nl.knaw.huygens.timbuctoo.core.dto;

import nl.knaw.huygens.timbuctoo.core.CollectionNameHelper;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Objects;

public class CreateCollection {
  private final String unprefixedEntityName;

  public CreateCollection(String unprefixedEntityName) {
    this.unprefixedEntityName = unprefixedEntityName;
  }

  public static CreateCollection forEntityTypeName(String unprefixedEntityTypeName) {
    return new CreateCollection(unprefixedEntityTypeName);
  }

  public static CreateCollection defaultCollection() {
    return forEntityTypeName(CollectionNameHelper.DEFAULT_COLLECTION_ENTITY_NAME);
  }

  public String getEntityTypeName(Vre vre) {
    return CollectionNameHelper.entityTypeName(unprefixedEntityName, vre);
  }

  public String getCollectionName(Vre vre) {
    return CollectionNameHelper.collectionName(unprefixedEntityName, vre);
  }

  public boolean isUnknownCollection() {
    return Objects.equals(unprefixedEntityName, CollectionNameHelper.DEFAULT_COLLECTION_ENTITY_NAME);
  }

  public String getRdfUri(Vre vre) {
    return CollectionNameHelper.rdfUri(unprefixedEntityName, vre);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
