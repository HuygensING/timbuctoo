package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

public class UriHelper {
  private final URI baseUri;

  public UriHelper(TimbuctooConfiguration configuration) {
    baseUri = UriBuilder.fromUri(configuration.getBaseUri()).build();
  }

  public URI makeUri(Class<?> resource, Map<String, String> map) {
    return makeUri(resource, map, ImmutableMap.of());
  }

  public URI makeUri(Class<?> resource, Map<String, String> map, Map<String, String> query) {
    // Create the URI based on the resource
    UriBuilder resourceUriBuilder = UriBuilder.fromResource(resource);
    query.entrySet().forEach(e -> resourceUriBuilder.queryParam(e.getKey(), e.getValue()));
    URI resourceUri = resourceUriBuilder.buildFromEncodedMap(map);

    // Make sure the URI starts with the base URI
    URI uri = UriBuilder.fromUri(resourceUri)
                        .scheme(baseUri.getScheme()).host(baseUri.getHost()).port(baseUri.getPort())
                        .replacePath(baseUri.getPath()).path(resourceUri.getPath())
                        .build();
    return uri;

  }

  public URI fromResourceUri(URI resourceUri) {
    URI uri = UriBuilder.fromUri(resourceUri)
                        .scheme(baseUri.getScheme()).host(baseUri.getHost()).port(baseUri.getPort())
                        .replacePath(baseUri.getPath()).path(resourceUri.getPath())
                        .build();
    return uri;
  }
}
