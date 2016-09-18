package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import org.apache.http.impl.client.CloseableHttpClient;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created on 2016-09-17 14:54.
 */
public class RsExplorer extends AbstractUriExplorer {

  public RsExplorer(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    super(httpClient, rsContext);
  }

  @Override
  public Result<RsRoot> explore(URI uri, ResultIndex index) {
    Result<RsRoot> result = execute(uri, rsConverter);
    index.add(result);

    // rs:ln rel="up" -> points to parent document
    String parentLink = result.getContent().map(rsRoot -> rsRoot.getLink("up")).orElse(null);
    if (parentLink != null && !index.contains(parentLink)) {
      try {
        URI parentUri = new URI(parentLink);
        Result<RsRoot> parentResult = explore(parentUri, index);
        result.setParent(parentResult);
      } catch (URISyntaxException e) {
        index.addInvalidUri(parentLink);
      }
    }


    return result;
  }
}
