package nl.knaw.huygens.timbuctoo.util;

import com.fasterxml.jackson.annotation.JsonValue;

public record Graph(String uri) {
  public Graph(String uri) {
    this.uri = uri != null ? uri : "";
  }

  @Override
  @JsonValue
  public String uri() {
    return uri;
  }

  public boolean isDefaultGraph() {
    return uri.isBlank();
  }
}
