package nl.knaw.huygens.timbuctoo.server;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class UriHelper {
  private final URI baseUri;

  public UriHelper(String baseUri) {
    this.baseUri = UriBuilder.fromUri(baseUri).build();
  }

  public URI fromResourceUri(URI resourceUri) {
    return UriBuilder.fromUri(resourceUri)
                     .userInfo(baseUri.getUserInfo())
                     .scheme(baseUri.getScheme())
                     .host(baseUri.getHost())
                     .port(baseUri.getPort())
                     .replacePath(baseUri.getPath()).path(resourceUri.getPath())
                     .build();
  }

}
