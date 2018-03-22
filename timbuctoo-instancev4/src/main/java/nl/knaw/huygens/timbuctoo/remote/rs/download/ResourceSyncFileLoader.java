package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

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

  private static final Map<String, String> MIME_TYPE_FOR_EXTENSION = ImmutableMap.<String, String>builder()
    .put("ttl", "text/turtle")
    .put("rdf", "application/rdf+xml")
    .put("nt", "application/n-triples")
    .put("jsonld", "application/ld+json")
    .put("owl", "application/owl+xml")
    .put("trig", "application/trig")
    .put("nq", "application/n-quads")
    .put("trix", "application/trix+xml")
    .put("trdf", "application/rdf+thrift")
    .build();
  private static final Logger LOG = getLogger(ResourceSyncFileLoader.class);
  private final RemoteFileRetriever remoteFileRetriever;
  private final ObjectMapper objectMapper;

  public ResourceSyncFileLoader(CloseableHttpClient httpClient) {
    objectMapper = new XmlMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    remoteFileRetriever = new RemoteFileRetriever(httpClient);
  }

  public ResourceSyncFileLoader(RemoteFileRetriever remoteFileRetriever) {
    objectMapper = new XmlMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.remoteFileRetriever = remoteFileRetriever;
  }


  //FIXME: maybe we should just store everything compressed
  public static InputStream maybeDecompress(InputStream input) throws IOException {
    final PushbackInputStream pb = new PushbackInputStream(input, 2);

    int header = pb.read();
    if (header == -1) {
      return pb;
    }

    int firstByte = pb.read();
    if (firstByte == -1) {
      pb.unread(header);
      return pb;
    }

    pb.unread(new byte[]{(byte) header, (byte) firstByte});

    header = (firstByte << 8) | header;

    if (header == GZIPInputStream.GZIP_MAGIC) {
      return new GZIPInputStream(pb);
    } else {
      return pb;
    }
  }

  public Stream<RemoteFile> loadFiles(String capabilityListUri) throws IOException {
    List<UrlItem> itemList = getRsFile(capabilityListUri).getItemList();

    Stream<RemoteFile> remoteFileStream = itemList.stream()
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
      .filter(item -> {
        String datasetNamePattern = ".*dataset.(nq|rdf)";
        return item.getLoc().matches(datasetNamePattern);
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
      .map(resource -> {
        try {
          return RemoteFile.create(resource.getLeft(), remoteFileRetriever.getFile(
            resource.getLeft()),
            resource.getRight()
          );
        } catch (IOException e) {
          throw new RuntimeUpgrader(e);
        }
      });
    LOG.info("Resources found");
    return remoteFileStream;
  }

  private UrlSet getRsFile(String url) throws IOException {
    LOG.info("getRsFile '{}'", url);
    return objectMapper.readValue(IOUtils.toString(remoteFileRetriever.getFile(url))
      .replace("rs.md", "rs:md"), UrlSet.class);
  }

  static class RemoteFileRetriever {
    private final HttpClient httpClient;

    private RemoteFileRetriever(HttpClient httpClient) {
      this.httpClient = httpClient;
    }

    public InputStream getFile(String url) throws IOException {
      InputStream content = httpClient.execute(new HttpGet(url)).getEntity().getContent();
      if (content != null) {
        return maybeDecompress(content);
      } else {
        return new ByteArrayInputStream(new byte[0]);
      }
    }
  }

  private class RuntimeUpgrader extends RuntimeException {
    private RuntimeUpgrader(Throwable cause) {
      super(cause);
    }
  }
}
