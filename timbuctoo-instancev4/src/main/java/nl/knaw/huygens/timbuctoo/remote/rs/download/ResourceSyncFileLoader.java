package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Browses the resourcesync files and loads all resources that are referenced
 * <p/>
 * A resource might not be downloadable, in that case loadFiles will throw and abort in an unfinished state.
 * You can provide a httpClient that automatically retries failed requests and that waits a long time
 * before returning a timeout to limit the amount of failures.
 */
public class ResourceSyncFileLoader {

  private static final Logger LOG = getLogger(ResourceSyncFileLoader.class);
  private final CloseableHttpClient httpClient;
  protected final ObjectMapper objectMapper;

  public ResourceSyncFileLoader(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
    objectMapper = new ObjectMapper();
  }

  private Urlset getRsFile(String url) throws IOException {
    return objectMapper.readValue(getFile(url), Urlset.class);
  }

  private InputStream getFile(String url) throws IOException {
    InputStream content = httpClient.execute(new HttpGet(url)).getEntity().getContent();
    if (content != null) {
      return content;
    } else {
      return new ByteArrayInputStream(new byte[0]);
    }
  }

  public Stream<RemoteFile> loadFiles(String capabilityListUri) throws IOException {
    try {
      return
        getRsFile(capabilityListUri)
          .getItemList().stream()
          .filter(url -> url
            .getMetadata()
            .flatMap(RsMd::getCapability)
            .map(cap -> cap.equals(Capability.RESOURCELIST.getXmlValue()))
            .orElse(false)
          )
          .map(resourceListUrl -> {
            try {
              return getRsFile(resourceListUrl.getLoc());
            } catch (IOException e) {
              throw new RuntimeUpgrader(e);
            }
          })
          .flatMap(resourceList -> resourceList.getItemList().stream())
          .map(RsItem::getLoc)
          .map(resource -> {
            try {
              return RemoteFile.create(resource, getFile(resource));
            } catch (IOException e) {
              throw new RuntimeUpgrader(e);
            }
          });
    } catch (RuntimeUpgrader e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else {
        throw e;
      }
    }
  }

  private class RuntimeUpgrader extends RuntimeException {
    private RuntimeUpgrader(Throwable cause) {
      super(cause);
    }
  }
}
