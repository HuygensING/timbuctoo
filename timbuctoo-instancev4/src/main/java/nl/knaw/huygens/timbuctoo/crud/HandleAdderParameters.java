package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.net.URI;
import java.util.UUID;

public class HandleAdderParameters {
  private final UUID vertexId;
  private final int rev;
  private final URI url;
  private final int retries;


  public HandleAdderParameters(UUID vertexId, int rev, URI url) {
    this.vertexId = vertexId;
    this.rev = rev;
    this.url = url;
    this.retries = 0;
  }

  @JsonCreator
  public HandleAdderParameters(@JsonProperty("vertexId") UUID vertexId, @JsonProperty("rev") int rev,
                               @JsonProperty("url") URI url, @JsonProperty("retries") int retries) {
    this.vertexId = vertexId;
    this.url = url;
    this.retries = retries;
    this.rev = rev;
  }

  public UUID getVertexId() {
    return vertexId;
  }

  public URI getUrl() {
    return url;
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
}
