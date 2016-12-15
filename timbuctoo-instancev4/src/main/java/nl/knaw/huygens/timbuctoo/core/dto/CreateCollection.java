package nl.knaw.huygens.timbuctoo.core.dto;

import nl.knaw.huygens.timbuctoo.database.CollectionNameHelper;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Objects;

public class CreateCollection {
  public static final String DEFAULT_COLLECTION_NAME = "unknown";
  private final String unprefixedEntityName;

  public CreateCollection(String unprefixedEntityName) {
    this.unprefixedEntityName = unprefixedEntityName;
  }

  public static CreateCollection forEntityTypeName(String unprefixedEntityTypeName) {
    return new CreateCollection(unprefixedEntityTypeName);
  }

  public static CreateCollection defaultCollection() {
    return forEntityTypeName(DEFAULT_COLLECTION_NAME);
  }

  public String getEntityTypeName(Vre vre) {
    return CollectionNameHelper.entityTypeName(unprefixedEntityName, vre);
  }

  public String getCollectionName(Vre vre) {
    return CollectionNameHelper.collectionName(unprefixedEntityName, vre);
  }

  public boolean isUknownCollection() {
    return Objects.equals(unprefixedEntityName, DEFAULT_COLLECTION_NAME);
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
