package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Explore a ResourceSync source. ResourceSync documents form a network with 'up' and 'index' links to
 * parent documents and 'url/loc' or 'sitemap/loc' links to child documents.
 * This class enables exploration of, and navigation through such a network.
 *
 * <q>
 * ResourceSync provides three ways for a Destination to discover whether and how a Source supports ResourceSync.
 * <ul>
 * <li>6.3.2 ResourceSync Well-Known URI</li>
 * <li>6.3.3 Links</li>
 * <li>6.3.4 robots.txt</li>
 * </ul>
 * </q>
 *
 * <ol>
 * <li>The <i>Well-Known URI</i> points to a document with capability 'description'.</li>
 * <li>The links in headers point to documents with capability 'capabilitylist'.</li>
 * <li>The links in <i>robots.txt</i> point to documents with capability 'resourcelist'.</li>
 * </ol>
 * We will add forth way: a url that points to a document of whatever capability.
 *
 * @see <a href="http://www.openarchives.org/rs/1.0/resourcesync#Discovery">
 * http://www.openarchives.org/rs/1.0/resourcesync#Discovery</a>
 */
public class Expedition {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Expedition.class);

  private static final String WELL_KNOWN_PATH = "/.well-known/resourcesync";
  private static final String ROBOTS_TXT = "/robots.txt";
  private final CloseableHttpClient httpClient;
  private final ResourceSyncContext rsContext;

  public Expedition(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    this.httpClient = httpClient;
    this.rsContext = rsContext;
  }

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

  /**
   * Gather ResourceSync Framework documents from a source in ResultIndexes.
   *
   * @param url        the starting url to explore
   * @param authString authorization token, optional
   * @return List of resultIndexes of the exploration
   * @throws URISyntaxException   if the url could not be converted to a URI.
   * @throws InterruptedException at Executor interrupts.
   */
  public List<ResultIndex> explore(String url, String authString) throws URISyntaxException, InterruptedException {
    URI uri = new URI(url);

    ExecutorService executor = Executors.newWorkStealingPool();

    List<Callable<ResultIndex>> callables = new ArrayList<>();
    callables.add(() -> exploreWellKnown(uri, authString));
    callables.add(() -> exploreLinks(uri));
    callables.add(() -> exploreRobotsTxt(uri));
    callables.add(() -> exploreRsDocumentUri(uri));

    return executor.invokeAll(callables)
      .stream()
      .map(future -> {
        try {
          return future.get();
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      })
      .collect(Collectors.toList());
  }

  public ResultIndex exploreAndMerge(String url, String authString) throws URISyntaxException, InterruptedException {
    // not sure how/whether this can be done on the stream of explore..
    List<ResultIndex> indexes = explore(url, authString);

    ResultIndex resultIndex = new ResultIndex();
    indexes.forEach(ri -> resultIndex.merge(ri));

    return resultIndex;
  }

  private ResultIndex exploreWellKnown(URI uri, String authString) {
    ResultIndex index = new ResultIndex();
    RsExplorer explorer = new RsExplorer(httpClient, rsContext);
    explorer.explore(createWellKnownUri(uri), index, authString);
    //LOG.info("exploreWellKnown. Found {} results with {}", index.listResultsWithContent().size(), uri.toString());
    return index;
  }

  private ResultIndex exploreLinks(URI uri) {
    ResultIndex index = new ResultIndex();
    LinkExplorer explorer = new LinkExplorer(httpClient, rsContext, LinkExplorer.linkReader);
    explorer.explore(uri, index, null);
    //LOG.info("exploreLinks. Found {} results with {}", index.listResultsWithContent().size(), uri.toString());
    return index;
  }

  private ResultIndex exploreRobotsTxt(URI uri) {
    ResultIndex index = new ResultIndex();
    LinkExplorer explorer = new LinkExplorer(httpClient, rsContext, LinkExplorer.robotsReader);
    explorer.explore(createRobotsUri(uri), index, null);
    //LOG.info("exploreRobotsTxt. Found {} results with {}", index.listResultsWithContent().size(), uri.toString());
    return index;
  }

  private ResultIndex exploreRsDocumentUri(URI uri) {
    ResultIndex index = new ResultIndex();
    RsExplorer explorer = new RsExplorer(httpClient, rsContext);
    explorer.explore(uri, index, null);
    //LOG.info("exploreRsDocumentUri. Found {} results with {}", index.listResultsWithContent().size(), uri.toString());
    return index;
  }
}
