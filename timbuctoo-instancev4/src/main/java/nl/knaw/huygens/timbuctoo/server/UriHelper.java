package nl.knaw.huygens.timbuctoo.server;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class UriHelper {
  private final URI baseUri;

  public UriHelper(TimbuctooConfiguration configuration) {
    baseUri = UriBuilder.fromUri(configuration.getBaseUri()).build();
  }
  
  public URI fromResourceUri(URI resourceUri) {
    URI uri = UriBuilder.fromUri(resourceUri)
                        .scheme(baseUri.getScheme()).host(baseUri.getHost()).port(baseUri.getPort())
                        .replacePath(baseUri.getPath()).path(resourceUri.getPath())
                        .build();
    return uri;
  }
}
