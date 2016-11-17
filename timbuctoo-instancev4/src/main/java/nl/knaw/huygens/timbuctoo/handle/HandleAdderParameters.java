package nl.knaw.huygens.timbuctoo.handle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.UUID;

public class HandleAdderParameters {
  private final UUID vertexId;
  private final int rev;
  private final String collectionName;
  private final int retries;


  public HandleAdderParameters(String collectionName, UUID vertexId, int rev) {
    this.vertexId = vertexId;
    this.rev = rev;
    this.collectionName = collectionName;
    this.retries = 0;
  }

  @JsonCreator
  public HandleAdderParameters(
    @JsonProperty("collectionName") String collectionName,
    @JsonProperty("vertexId") UUID vertexId,
    @JsonProperty("rev") int rev,
    @JsonProperty("retries") int retries
  ) {
    this.vertexId = vertexId;
    this.collectionName = collectionName;
    this.retries = retries;
    this.rev = rev;
  }

  public UUID getVertexId() {
    return vertexId;
  }

  public int getRetries() {
    return retries;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public int getRev() {
    return rev;
  }

  public String getCollectionName() {
    return collectionName;
  }
}
