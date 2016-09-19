package nl.knaw.huygens.timbuctoo.remote.rs;


import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URISyntaxException;

public class ResourcesyncService {

  private CloseableHttpClient httpClient;
  private ResourceSyncContext rsContext;

  public ResourcesyncService(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public RsRoot explore(String url) throws URISyntaxException, IOException {
    RsRoot result = null;
    return result;
  }

}
