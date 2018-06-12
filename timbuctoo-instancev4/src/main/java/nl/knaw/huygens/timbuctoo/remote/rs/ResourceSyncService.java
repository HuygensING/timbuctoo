package nl.knaw.huygens.timbuctoo.remote.rs;


import nl.knaw.huygens.timbuctoo.remote.rs.discover.Expedition;
import nl.knaw.huygens.timbuctoo.remote.rs.view.FrameworkBase;
import nl.knaw.huygens.timbuctoo.remote.rs.view.Interpreter;
import nl.knaw.huygens.timbuctoo.remote.rs.view.SetListBase;
import nl.knaw.huygens.timbuctoo.remote.rs.view.TreeBase;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import org.apache.http.impl.client.CloseableHttpClient;

import java.net.URISyntaxException;

public class ResourceSyncService {

  private final CloseableHttpClient httpClient;
  private final ResourceSyncContext rsContext;

  public ResourceSyncService(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    this.httpClient = httpClient;
    this.rsContext = rsContext;
  }

  public SetListBase listSets(String url, Interpreter interpreter, String authString)
    throws URISyntaxException, InterruptedException {

    Expedition expedition = new Expedition(httpClient, rsContext);
    return new SetListBase(expedition.exploreAndMerge(url,""), interpreter);
  }

  public FrameworkBase getFramework(String url, Interpreter interpreter)
    throws URISyntaxException, InterruptedException {
    Expedition expedition = new Expedition(httpClient, rsContext);
    return new FrameworkBase(expedition.exploreAndMerge(url, null), interpreter);
  }

  public TreeBase getTree(String url, Interpreter interpreter) throws URISyntaxException, InterruptedException {
    Expedition expedition = new Expedition(httpClient, rsContext);
    return new TreeBase(expedition.exploreAndMerge(url, null), interpreter);
  }

}
