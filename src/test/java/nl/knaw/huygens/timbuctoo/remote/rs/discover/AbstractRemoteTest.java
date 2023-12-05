package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.socket.PortFactory;

import java.net.URI;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public abstract class AbstractRemoteTest {

  private static ClientAndServer mockServer;
  private static int port;

  private static ResourceSyncContext rsContext;
  private static CloseableHttpClient httpclient;

  @BeforeAll
  public static void initialize() throws Exception {
    port = PortFactory.findFreePort();
    mockServer = startClientAndServer(port);
    rsContext = new ResourceSyncContext();
    httpclient = HttpClients.createDefault();
  }

  @AfterAll
  public static void tearDown() {
    mockServer.stop();
  }

  public static ClientAndServer getMockServer() {
    return mockServer;
  }

  public static int getPort() {
    return port;
  }

  public static ResourceSyncContext getRsContext() {
    return rsContext;
  }

  public static CloseableHttpClient getHttpclient() {
    return httpclient;
  }

  public static String composePath(String path) {
    if (path.startsWith("/")) {
      return "http://localhost:" + port + path;
    } else {
      return "http://localhost:" + port + "/" + path;
    }
  }

  public static URI composeUri(String path) {
    return URI.create(composePath(path));
  }
}
