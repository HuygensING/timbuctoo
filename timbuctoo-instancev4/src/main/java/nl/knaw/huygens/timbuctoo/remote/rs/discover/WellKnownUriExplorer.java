package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import org.apache.http.impl.client.CloseableHttpClient;

import java.net.URI;

/**
 * Explores the well-known URI.
 *
 * <q>
 * A Source must publish a Source Description, ... and it should be published at
 * the well-known URI [RFC 5785] /.well-known/resourcesync ... The Source Description document
 * enumerates a Source's Capability Lists and as such is an appropriate entry point for
 * Destinations interested in understanding a Source's capabilities.
 * </q>
 *
 * @see <a href="http://www.openarchives.org/rs/1.0/resourcesync#wellknown">
 *   http://www.openarchives.org/rs/1.0/resourcesync#wellknown</a>
 */
public class WellKnownUriExplorer extends AbstractUriExplorer {

  private static final String WELL_KNOWN_PATH = ".well-known/resourcesync";

  private static final String EXPECTED_CAPABILITY = "description";

  public WellKnownUriExplorer(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    super(httpClient, rsContext);
  }

  @Override
  public Result<RsRoot> explore(URI uri, ResultIndex index) {
    String uriString = uri.toString();
    if (!uriString.endsWith(WELL_KNOWN_PATH)) {
      if (!uriString.endsWith("/")) {
        uriString += "/";
      }
      uriString += WELL_KNOWN_PATH;
    }
    URI wellKnownUri = URI.create(uriString);

    Result<RsRoot> result = execute(wellKnownUri, rsConverter);

    String capa = result.getContent().map(RsRoot::getMetadata)
      .flatMap(RsMd::getCapability).orElse("Invalid capability");
    System.out.println(capa);

    if (result.getContent().isPresent()) {
      // it should have capability 'description' and it can be either
      //   - a urlset with direct pointers to capability-lists
      //   - a sitemapindex with pointers to other descriptions.
      if (EXPECTED_CAPABILITY.equals(capa)) {

      } else {
        result.setError(new RemoteResourceSyncFrameworkException(
          String.format("unexpected capability: '%s'. Expected capability is '%s'",
          capa, EXPECTED_CAPABILITY)));
      }
    }
    return result;
  }
}
