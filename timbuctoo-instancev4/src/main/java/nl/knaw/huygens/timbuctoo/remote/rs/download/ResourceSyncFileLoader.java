package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;
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
  private static final Map<String, String> MIME_TYPE_FOR_EXTENSION = ImmutableMap.<String, String>builder()
    .put("ttl", "text/turtle")
    .put("rdf", "application/rdf+xml")
    .put("nt", "application/n-triples")
    .put("jsonld", "application/ld+json")
    .put("owl", "application/owl+xml")
    .put("trig", "text/trig")
    .put("nq", "application/n-quads")
    .put("trix", "application/trix+xml")
    .put("trdf", "application/rdf+thrift")
    .build();
  private static final Logger LOG = getLogger(ResourceSyncFileLoader.class);
  protected final ObjectMapper objectMapper;
  private final CloseableHttpClient httpClient;

  public ResourceSyncFileLoader(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
    objectMapper = new XmlMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private UrlSet getRsFile(String url) throws IOException {
    LOG.info("getRsFile '{}'", url);
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
    Stream<RemoteFile> remoteFileStream = getRsFile(capabilityListUri)
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
      .flatMap(resourceList -> {
        LOG.info("map resource list");
        return resourceList.getItemList().stream();
      })
      .map(item -> tuple(item.getLoc(), item.getMetadata()))
      .map(item -> {
        if (item.getRight() != null) {
          return tuple(item.getLeft(), item.getRight().getMimeType());
        } else {
          String extension = item.getLeft().substring(item.getLeft().lastIndexOf(".") + 1);
          return tuple(item.getLeft(), MIME_TYPE_FOR_EXTENSION.get(extension));
        }
      })
      .filter(item -> SUPPORTED_MIME_TYPES.contains(item.getRight()))
      .map(resource -> {
        try {
          return RemoteFile.create(resource.getLeft(), getFile(resource.getLeft()), resource.getRight());
        } catch (IOException e) {
          throw new RuntimeUpgrader(e);
        }
      });
    LOG.info("Resources found");
    return remoteFileStream;
  }

  private class RuntimeUpgrader extends RuntimeException {
    private RuntimeUpgrader(Throwable cause) {
      super(cause);
    }
  }
}
