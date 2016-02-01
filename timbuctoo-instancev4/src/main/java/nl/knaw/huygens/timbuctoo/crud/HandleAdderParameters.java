package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.net.URI;

public class HandleAdderParameters {
  @JsonProperty
  private final Object vertexId;
  @JsonProperty
  private final URI url;
  @JsonProperty
  private final int retries;


  public HandleAdderParameters(Object vertexId, URI url) {
    this.vertexId = vertexId;
    this.url = url;
    this.retries = 0;
  }

  public HandleAdderParameters(Object vertexId, URI url, int retries) {
    this.vertexId = vertexId;
    this.url = url;
    this.retries = retries;
  }

  public Object getVertexId() {
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
}
