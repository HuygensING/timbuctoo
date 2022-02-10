package nl.knaw.huygens.timbuctoo.v5.util;

import com.fasterxml.jackson.annotation.JsonValue;

public class Graph {
  final String uri;

  public Graph(String uri) {
    this.uri = uri != null ? uri : "";
  }

  @JsonValue
  public String getUri() {
    return uri;
  }

  public boolean isDefaultGraph() {
    return uri.isBlank();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    return ((Graph) other).getUri().equals(uri);
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public String toString() {
    return uri;
  }
}
