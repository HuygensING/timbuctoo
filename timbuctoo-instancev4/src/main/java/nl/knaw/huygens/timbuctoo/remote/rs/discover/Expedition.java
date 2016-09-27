package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import org.apache.http.impl.client.CloseableHttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Explore a ResourceSync source.
 *
 * <q>
 * ResourceSync provides three ways for a Destination to discover whether and how a Source supports ResourceSync.
 * <ul>
 *  <li>6.3.2 ResourceSync Well-Known URI</li>
 *  <li>6.3.3 Links</li>
 *  <li>6.3.4 robots.txt</li>
 * </ul>
 * </q>
 *
 * @see <a href="http://www.openarchives.org/rs/1.0/resourcesync#Discovery">
 *   http://www.openarchives.org/rs/1.0/resourcesync#Discovery</a>
 */
public class Expedition {

  private static final String WELL_KNOWN_PATH = "/.well-known/resourcesync";
  private static final String ROBOTS_TXT = "/robots.txt";

  public static URI createWellKnownUri(URI uri) {
    URI wellKnownUri = uri;
    if (!uri.getPath().endsWith(WELL_KNOWN_PATH)) {
      wellKnownUri = uri.resolve(WELL_KNOWN_PATH);
    }
    return wellKnownUri;
  }

  public static URI createRobotsUri(URI uri) {
    URI robotsUri = uri;
    if (!uri.getPath().endsWith(ROBOTS_TXT)) {
      robotsUri = uri.resolve(ROBOTS_TXT);
    }
    return robotsUri;
  }

  private final CloseableHttpClient httpClient;
  private final ResourceSyncContext rsContext;


  public Expedition(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    this.httpClient = httpClient;
    this.rsContext = rsContext;
  }

  /**
   * Gather ResourceSync Framework documents from a source in a ResultIndex.
   *
   * @param url the starting url to explore
   * @return the resultIndex of the exploration
   * @throws URISyntaxException if the url could not be converted to a URI.
   */
  public List<ResultIndex> explore(String url) throws URISyntaxException, InterruptedException {
    URI uri = new URI(url);

    ExecutorService executor = Executors.newWorkStealingPool();

    List<Callable<ResultIndex>> callables = new ArrayList<>();
    callables.add(() -> exploreWellKnown(uri));
    callables.add(() -> exploreLinks(uri));
    callables.add(() -> exploreRobotsTxt(uri));
    callables.add(() -> exploreRsDocumentUri(uri));

    List<ResultIndex> indexes = executor.invokeAll(callables)
      .stream()
      .map(future -> {
        try {
          return future.get();
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      })
      .collect(Collectors.toList());

    return indexes;
  }

  private ResultIndex exploreWellKnown(URI uri) {
    ResultIndex index = new ResultIndex();
    RsExplorer explorer = new RsExplorer(httpClient, rsContext);
    explorer.explore(createWellKnownUri(uri), index);
    return index;
  }

  private ResultIndex exploreLinks(URI uri) {
    ResultIndex index = new ResultIndex();
    LinkExplorer explorer = new LinkExplorer(httpClient, rsContext, LinkExplorer.linkReader);
    explorer.explore(uri, index);
    return index;
  }

  private ResultIndex exploreRobotsTxt(URI uri) {
    ResultIndex index = new ResultIndex();
    LinkExplorer explorer = new LinkExplorer(httpClient, rsContext, LinkExplorer.robotsReader);
    explorer.explore(createRobotsUri(uri), index);
    return index;
  }

  private ResultIndex exploreRsDocumentUri(URI uri) {
    ResultIndex index = new ResultIndex();
    RsExplorer explorer = new RsExplorer(httpClient, rsContext);
    explorer.explore(uri, index);
    return index;
  }
}
