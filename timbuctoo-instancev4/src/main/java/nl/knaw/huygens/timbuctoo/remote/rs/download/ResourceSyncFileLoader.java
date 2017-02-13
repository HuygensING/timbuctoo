package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
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

  private static final Set<String> SUPPORTED_MIME_TYPES = Sets.newHashSet(
    "text/turtle",
    "application/rdf+xml",
    "application/n-tripples",
    "application/ld+json",
    "application/owl+xml",
    "text/trig",
    "application/n-quads",
    "application/trix+xml",
    "application/rdf+thrift"
  );
  private static final Logger LOG = getLogger(ResourceSyncFileLoader.class);
  private final CloseableHttpClient httpClient;
  protected final ObjectMapper objectMapper;

  public ResourceSyncFileLoader(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
    objectMapper = new XmlMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private UrlSet getRsFile(String url) throws IOException {
    return objectMapper.readValue(getFile(url), UrlSet.class);
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
            .getMetadata().getCapability().equals(Capability.RESOURCELIST.getXmlValue())
          )
          .map(resourceListUrl -> {
            try {
              return getRsFile(resourceListUrl.getLoc());
            } catch (IOException e) {
              throw new RuntimeUpgrader(e);
            }
          })
          .flatMap(resourceList -> resourceList.getItemList().stream())
          .filter(item -> SUPPORTED_MIME_TYPES.contains(item.getMetadata().getMimeType()))
          .map(resource -> {
            try {
              return RemoteFile.create(resource.getLoc(), getFile(resource.getLoc()), resource.getMetadata());
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
