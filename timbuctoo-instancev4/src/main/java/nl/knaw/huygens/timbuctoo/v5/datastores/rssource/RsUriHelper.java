package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.RsEndpoint;

import static javax.ws.rs.core.UriBuilder.fromResource;

/**
 * Created on 2018-01-31 13:24.
 */
public class RsUriHelper {

  private final UriHelper uriHelper;

  private static final String RESOURCE_SYNC_PATH = fromResource(RsEndpoint.class).build().getPath();
  private static final String WELL_KNOWN_RESOURCESYNC = ".well-known/resourcesync";

  public RsUriHelper(UriHelper uriHelper) {
    this.uriHelper = uriHelper;
  }

  public String uriForRsDocument(DataSetMetaData dataSetMetaData, Capability capability) {
    return uriForFilename(dataSetMetaData, capability.getFilename());
  }

  public String uriForFilename(DataSetMetaData dataSetMetaData, String filename) {
    return String.format("%s/%s/%s/%s/%s", uriHelper.getBaseUri(), RESOURCE_SYNC_PATH, dataSetMetaData.getOwnerId(),
      dataSetMetaData.getDataSetId(), filename);
  }

  public String uriForFilename(String filename) {
    return String.format("%s/%s/%s", uriHelper.getBaseUri(), RESOURCE_SYNC_PATH, filename);
  }

  public String uriForWellKnownResourceSync() {
    return String.format("%s/%s", uriHelper.getBaseUri(), WELL_KNOWN_RESOURCESYNC);
  }
}
