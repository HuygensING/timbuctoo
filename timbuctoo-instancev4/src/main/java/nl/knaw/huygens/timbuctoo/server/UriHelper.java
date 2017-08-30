package nl.knaw.huygens.timbuctoo.server;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class UriHelper {
  private URI baseUri;

  @JsonCreator
  public UriHelper(URI baseUri) {
    this.baseUri = baseUri;
  }

  public URI fromResourceUri(URI resourceUri) {
    URI baseUri = UriBuilder.fromUri(this.baseUri).build();
    return UriBuilder.fromUri(resourceUri)
                     .userInfo(baseUri.getUserInfo())
                     .scheme(baseUri.getScheme())
                     .host(baseUri.getHost())
                     .port(baseUri.getPort())
                     .replacePath(baseUri.getPath()).path(resourceUri.getPath())
                     .build();
  }

  void setBaseUri(URI baseUri) {
    this.baseUri = baseUri;
  }

  boolean hasDynamicBaseUrl() {
    return "http://0.0.0.0:0".equals(this.baseUri.toString());
  }

  public URI getBaseUri() {
    return baseUri;
  }
}
