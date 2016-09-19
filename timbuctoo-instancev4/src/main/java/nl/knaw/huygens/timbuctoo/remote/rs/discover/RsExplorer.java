package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import org.apache.http.impl.client.CloseableHttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class RsExplorer extends AbstractUriExplorer {

  public RsExplorer(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    super(httpClient, rsContext);
  }

  @SuppressWarnings ("unchecked")
  @Override
  public Result<RsRoot> explore(URI uri, ResultIndex index) {
    Result<RsRoot> result = execute(uri, rsConverter);
    index.add(result);

    // rs:ln rel="up" -> points to parent document, a urlset.
    String parentLink = result.getContent().map(rsRoot -> rsRoot.getLink("up")).orElse(null);
    if (parentLink != null && !index.contains(parentLink)) {
      try {
        URI parentUri = new URI(parentLink);
        Result<RsRoot> parentResult = explore(parentUri, index);
        result.addParent(parentResult);
      } catch (URISyntaxException e) {
        index.addInvalidUri(parentLink);
      }
    }

    // rs:ln rel="index" -> points to parent index, a sitemapindex.
    String indexLink = result.getContent().map(rsRoot -> rsRoot.getLink("index")).orElse(null);
    if (indexLink != null && !index.contains(indexLink)) {
      try {
        URI indexUri = new URI(indexLink);
        Result<RsRoot> indexResult = explore(indexUri, index);
        result.addParent(indexResult);
      } catch (URISyntaxException e) {
        index.addInvalidUri(indexLink);
      }
    }

    // elements <url> or <sitemap> have the location of the children of result
    List<RsItem> itemList = result.getContent().map(RsRoot::getItemList).orElse(Collections.emptyList());
    for (RsItem item : itemList) {
      String childLink = item.getLoc();
      if (childLink != null && !index.contains(childLink)) {
        try {
          URI childUri = new URI(childLink);
          Result<RsRoot> childResult = explore(childUri, index);
          result.addChild(childResult);
        } catch (URISyntaxException e) {
          index.addInvalidUri(childLink);
        }
      }
    }

    return result;
  }

}
