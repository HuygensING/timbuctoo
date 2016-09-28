package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.sync.ResourceSet;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.SitemapItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Sitemapindex;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.UrlItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;
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
 * Explore a ResourceSync source. ResourceSync documents form a network with 'up' and 'index' links to
 * parent documents and 'url/loc' or 'sitemap/loc' links to child documents.
 * This class enables exploration of, and navigation through such a network.
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
 * <ol>
 *  <li>The <i>Well-Known URI</i> points to a document with capability 'description'.</li>
 *  <li>The links in headers point to documents with capability 'capabilitylist'.</li>
 *  <li>The links in <i>robots.txt</i> point to documents with capability 'resourcelist'.</li>
 * </ol>
 * We will add forth way: a url that points to a document of whatever capability.
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
   * Gather ResourceSync Framework documents from a source in ResultIndexes.
   *
   * @param url the starting url to explore
   * @return List of resultIndexes of the exploration
   * @throws URISyntaxException if the url could not be converted to a URI.
   * @throws InterruptedException at Executor interrupts.
   */
  public List<ResultIndex> explore(String url) throws URISyntaxException, InterruptedException {
    URI uri = new URI(url);

    ExecutorService executor = Executors.newWorkStealingPool();

    List<Callable<ResultIndex>> callables = new ArrayList<>();
    callables.add(() -> exploreWellKnown(uri));
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

  /**
   * List the values of the &lt;loc&gt; element of &lt;url&gt; elements of documents of type urlset with
   * the given capability.
   * @param url the url to explore
   * @param capability the capability of the documents from which locations will be extracted
   * @return List of values of the &lt;loc&gt; elements
   * @throws URISyntaxException if the url could not be converted to a URI.
   * @throws InterruptedException at Executor interrupts.
   */
  public List<String> listUrlLocations(String url, Capability capability)
      throws URISyntaxException, InterruptedException {

    List<ResultIndex> indexes = explore(url);

    return indexes.stream()
      .map(resultIndex -> resultIndex.getUrlsetResults(capability))
      .flatMap(List::stream)
      .map(urlsetResult -> urlsetResult.getContent().orElse(null))
      .map(Urlset::getItemList)
      .flatMap(List::stream)
      .map(UrlItem::getLoc)
      .collect(Collectors.toList());
  }

  /**
   * List the values of the &lt;loc&gt; element of &lt;sitemap&gt; elements of documents of type sitemapindex with
   * the given capability.
   * @param url the url to explore
   * @param capability the capability of the documents from which locations will be extracted
   * @return List of values of the &lt;loc&gt; elements
   * @throws URISyntaxException if the url could not be converted to a URI.
   * @throws InterruptedException at Executor interrupts.
   */
  public List<String> listSitemapLocations(String url, Capability capability)
      throws URISyntaxException, InterruptedException {

    List<ResultIndex> indexes = explore(url);

    return indexes.stream()
      .map(resultIndex -> resultIndex.getSitemapindexResults(capability))
      .flatMap(List::stream)
      .map(sitemapindexResult -> sitemapindexResult.getContent().orElse(null))
      .map(Sitemapindex::getItemList)
      .flatMap(List::stream)
      .map(SitemapItem::getLoc)
      .collect(Collectors.toList());
  }

  public List<ResourceSet> listGraphs(String url) throws URISyntaxException, InterruptedException {
    // the loc values in description have the graph names in base64:
    // http://192.168.99.100:8085/aHR0cDovL2V4YW1wbGUuY29tL2NsYXJpYWgK/capability-list.xml
    List<ResultIndex> indexes = explore(url);

    List<Result<Urlset>> results = indexes.stream()
      .map(resultIndex -> resultIndex.getUrlsetResults(Capability.DESCRIPTION))
      .flatMap(List::stream)
      .collect(Collectors.toList());

    List<ResourceSet> sets = new ArrayList<>();
    for (Result<Urlset> result : results) {
      ResourceSet set = new ResourceSet(result.getUri());
      sets.add(set);
    }
    return sets;
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
