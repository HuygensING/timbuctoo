package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.remote.rs.download.exceptions.CantRetrieveFileException;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsLn;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    .put("nqud", "application/vnd.timbuctoo-rdf.nquads_unified_diff")
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

  public RemoteFilesList getRemoteFilesList(String capabilityListUri, String authString)
    throws IOException, CantRetrieveFileException {
    List<UrlItem> capabilityList = getRsFile(capabilityListUri, authString).getItemList();

    List<RemoteFile> changes = new ArrayList<>();
    List<RemoteFile> resources = new ArrayList<>();

    for (UrlItem capabilityListItem : capabilityList) {
      if (capabilityListItem.getMetadata().getCapability().equals(Capability.CHANGELIST.getXmlValue())) {
        UrlSet rsFile = getRsFile(capabilityListItem.getLoc(), authString);

        String changeListExtension = ".*.nqud";

        for (UrlItem changeListItem : rsFile.getItemList()) {
          RsLn changeLink = changeListItem.getLink();
          if (changeLink != null) {
            if ((changeLink.getType().isPresent() &&
              changeLink.getType().get().equals(MIME_TYPE_FOR_EXTENSION.get("nqud"))) ||
              changeLink.getHref().matches(changeListExtension)) {
              Metadata changeMd = new Metadata();
              changeMd.setMimeType(changeLink.getType().get());
              changeMd.setDateTime(changeListItem.getMetadata().getDateTime());
              RemoteFile remoteFile = getRemoteFile(new Tuple<>(changeLink.getHref(), changeMd),
                authString);
              changes.add(remoteFile);
            }
          }
        }
      } else if (capabilityListItem.getMetadata().getCapability().equals(Capability.RESOURCELIST.getXmlValue())) {
        UrlSet rsFile = getRsFile(capabilityListItem.getLoc(), authString);

        for (UrlItem resourceListItem : rsFile.getItemList()) {
          if (MIME_TYPE_FOR_EXTENSION.values().contains(resourceListItem.getMetadata().getMimeType()) ||
            SupportedRdfResourceListExtensions.createFromFile(resourceListItem.getLoc()) != null) {
            resources.add(getRemoteFile(new Tuple<>(resourceListItem.getLoc(), resourceListItem.getMetadata()),
              authString));
          }
        }
      }
    }

    return new RemoteFilesList(changes, resources);
  }


  private RemoteFile getRemoteFile(Tuple<String, Metadata> item, String authString) {
    return RemoteFile.create(
      item.getLeft(),
      () -> {
        try {
          return remoteFileRetriever.getFile(item.getLeft(), authString);
        } catch (CantRetrieveFileException e) {
          throw e;
        }
      },
      getUrl(tuple(item.getLeft(), item.getRight())),
      item.getRight()
    );
  }

  private String getUrl(Tuple<String, Metadata> item) {
    if (item.getRight() != null) {
      return item.getRight().getMimeType();
    } else {
      String extension = item.getLeft().substring(item.getLeft().lastIndexOf(".") + 1);
      return MIME_TYPE_FOR_EXTENSION.get(extension);
    }
  }

  private UrlSet getRsFile(String url, String authString) throws IOException, CantRetrieveFileException {
    LOG.info("getRsFile '{}'", url);
    return objectMapper.readValue(IOUtils.toString(remoteFileRetriever.getFile(url, authString))
      .replace("rs.md", "rs:md"), UrlSet.class);
  }

  private enum SupportedRdfResourceListExtensions {
    NQ("nq"),
    TRIG("trig"),
    NT("nt"),
    TTL("ttl"),
    N3("n3"),
    RDF_XML("xml"),
    JSONLD("jsonld"),;

    private final String extension;

    SupportedRdfResourceListExtensions(String extension) {
      this.extension = extension;
    }

    public static SupportedRdfResourceListExtensions createFromFile(String fileName) {
      for (SupportedRdfResourceListExtensions rdfExtensions : SupportedRdfResourceListExtensions.values()) {
        if (fileName.endsWith("." + rdfExtensions.getExtension())) {
          return rdfExtensions;
        }
      }
      return null;
    }

    public String getExtension() {
      return extension;
    }
  }

  static class RemoteFilesList {
    private List<RemoteFile> changeList;
    private List<RemoteFile> resourceList;

    public RemoteFilesList(List<RemoteFile> changeList, List<RemoteFile> resourceList) {
      this.changeList = changeList;
      this.resourceList = resourceList;
    }

    public List<RemoteFile> getChangeList() {
      return changeList;
    }

    public List<RemoteFile> getResourceList() {
      return resourceList;
    }
  }

  static class RemoteFileRetriever {
    private final HttpClient httpClient;

    private RemoteFileRetriever(HttpClient httpClient) {
      this.httpClient = httpClient;
    }

    public InputStream getFile(String url, String authString) throws CantRetrieveFileException, IOException {
      HttpGet httpGet = new HttpGet(url);

      /*Timeout time is set to 100seconds to prevent socket timeout during changelist import*/
      httpGet.setConfig(RequestConfig.custom().setSocketTimeout(100000).build());
      if (authString != null) {
        httpGet.addHeader("Authorization", authString);
      }
      HttpResponse httpResponse = httpClient.execute(httpGet);
      if (httpResponse.getStatusLine().getStatusCode() == 200) {
        InputStream content = httpResponse.getEntity().getContent();
        if (content != null) {
          return maybeDecompress(content);
        } else {
          return new ByteArrayInputStream(new byte[0]);
        }
      }

      throw new CantRetrieveFileException(httpResponse.getStatusLine().getStatusCode(),
        httpResponse.getStatusLine().getReasonPhrase());
    }
  }

}

