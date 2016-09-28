package nl.knaw.huygens.timbuctoo.remote.rs;


import nl.knaw.huygens.timbuctoo.remote.rs.discover.Expedition;
import nl.knaw.huygens.timbuctoo.remote.rs.sync.ResourceSet;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ResourceSyncService {

  private final CloseableHttpClient httpClient;
  private final ResourceSyncContext rsContext;

  public ResourceSyncService(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    this.httpClient = httpClient;
    this.rsContext = rsContext;
  }

  public List<ResourceSet> listGraphs(String url) throws URISyntaxException, InterruptedException {
    Expedition expedition = new Expedition(httpClient, rsContext);
    return expedition.listGraphs(url);
  }

}
