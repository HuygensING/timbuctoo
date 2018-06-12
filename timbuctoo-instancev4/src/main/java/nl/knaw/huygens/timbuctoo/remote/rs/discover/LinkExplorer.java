package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.Function_WithExceptions;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Explores links that point to ResourceSync Framework Documents.
 *
 * <h3>1. Explore a HTML document URI.</h3>
 * <q>
 * A Capability List may be made discoverable by means of links provided either in an HTML document
 * [HTML Links, XHTML Links] or in an HTTP Link header [RFC 5988].
 * </q>
 *
 * <h3>2. Explore Sitemap links in robots.txt.</h3>
 * <q>
 *   A Resource List is a Sitemap and hence may be made discoverable via the established approach of adding
 *   a Sitemap directive to a Source's robots.txt file that has the URI of the Resource List as its value.
 *   If a Source supports multiple sets of resources, multiple directives may be added, one for each
 *   Resource List associated with a specific set of resources.
 * </q>
 * <q>
 *   In case a Source supports both regular Sitemaps and ResourceSync Sitemaps (Resource Lists) they
 *   may be made discoverable, again, by including multiple Sitemap directives...
 * </q>
 * In the latter case, how a destination can distinguish between regular Sitemaps and ResourceSync Sitemaps is
 * not clear from the specification. Convention in naming?
 *
 * @see <a href="http://www.openarchives.org/rs/1.0/resourcesync#disco_links>
 *   http://www.openarchives.org/rs/1.0/resourcesync#disco_links</a>
 *
 * @see <a href="http://www.openarchives.org/rs/1.0/resourcesync#robots">
 *   http://www.openarchives.org/rs/1.0/resourcesync#robots</a>
 */
public class LinkExplorer extends AbstractUriExplorer {

  private final ResourceSyncContext rsContext;
  private Function_WithExceptions<HttpResponse, List<String>, ?> responseReader;

  public LinkExplorer(CloseableHttpClient httpClient,
                      ResourceSyncContext rsContext,
                      Function_WithExceptions<HttpResponse, List<String>, ?> responseReader) {
    super(httpClient);
    this.rsContext = rsContext;
    this.responseReader = responseReader;
  }

  @Override
  public Result<LinkList> explore(URI uri, ResultIndex index, String authString) {
    Result<List<String>> stringResult = execute(uri, responseReader, authString);
    Result<LinkList> result = stringResult.map(stringListToLinkListConverter);
    result.getInvalidUris().addAll(result.getContent().map(LinkList::getInvalidUris).orElse(Collections.emptySet()));
    index.add(result);

    // All valid uris point to ResourceSync documents (at least they should)
    RsExplorer rsExplorer = new RsExplorer(getHttpClient(), rsContext);
    for (URI rsUri : result.getContent().orElse(new LinkList()).getValidUris()) {
      if (!index.contains(rsUri)) {
        Result<RsRoot> child = rsExplorer.explore(rsUri, index, null);
        result.addChild(child);
      }
    }
    return  result;
  }

  private Function<List<String>, LinkList> stringListToLinkListConverter = (stringList) -> {
    LinkList linkList = new LinkList();
    linkList.resolve(getCurrentUri(), stringList);
    return linkList;
  };

  static Function_WithExceptions<HttpResponse, List<String>, Exception> linkReader = (response) -> {

    // a webpage that contains a link to a Capability List in the <head> section
    // http://www.openarchives.org/rs/1.0/resourcesync#ex_9
    InputStream inStream = response.getEntity().getContent();
    String charset = getCharset(response);
    Document doc = Jsoup.parse(inStream, charset, "");

    Elements elements = doc.head().getElementsByTag("link");
    List<String> uriList = new ArrayList<>();
    for (Element link : elements) {
      if ("resourcesync".equals(link.attr("rel"))) {
        uriList.add(link.attr("href"));

      }
    }

    // and/or a link is introduced in the HTTP Link header of a page
    // http://www.openarchives.org/rs/1.0/resourcesync#ex_10
    Pattern pattern = Pattern.compile("<(.*?)>");
    Header[] headers = response.getHeaders("Link");
    for (Header header : headers) {
      String value = header.getValue();
      if (value.contains("resourcesync")) {
        Matcher match = pattern.matcher(value);
        if (match.find()) {
          uriList.add(match.group(1));
        }
      }
    }
    return uriList;
  };

  static  Function_WithExceptions<HttpResponse, List<String>, Exception> robotsReader = (response) -> {
    String text = IOUtils.toString(response.getEntity().getContent(), getCharset(response));
    Matcher match = Pattern.compile("(?m)^Sitemap: (.*?)$").matcher(text);
    List<String> uriList = new ArrayList<>();
    while (match.find()) {
      uriList.add(match.group(1));
    }
    return uriList;
  };

}
