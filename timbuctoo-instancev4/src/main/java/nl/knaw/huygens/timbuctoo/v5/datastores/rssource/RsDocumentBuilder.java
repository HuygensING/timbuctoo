package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsLn;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.UrlItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto.FileSystemCachedFile;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.apache.commons.lang3.tuple.Pair;

import javax.activation.MimeType;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Optional;

/**
 * Created on 2018-01-31 09:41.
 */
public class RsDocumentBuilder {

  public static final String SOURCE_DESCRIPTION_PATH = "sourceDescription.xml";
  private static final String REL_DESCRIBED_BY = "describedby";
  private static final String REL_UP = "up";
  private static final String DESCRIPTION_FILENAME = "description.xml";
  private static final String DESCRIPTION_TYPE = "application/rdf+xml";

  private final DataSetRepository dataSetRepository;
  private final ResourceSync resourceSync;
  private final RsUriHelper rsUriHelper;
  // Can be dismissed on dynamic creation of resourcelist
  private final UriHelper uriHelper;

  public RsDocumentBuilder(DataSetRepository dataSetRepository, ResourceSync resourceSync, UriHelper uriHelper) {
    this.dataSetRepository = dataSetRepository;
    this.resourceSync = resourceSync;
    rsUriHelper = new RsUriHelper(uriHelper);
    // Can be dismissed on dynamic creation of resourcelist
    this.uriHelper = uriHelper;
  }

  // Can be dismissed on dynamic creation of resourcelist
  public UriHelper getUriHelper() {
    return this.uriHelper;
  }

  public Urlset getSourceDescription(@Nullable User user) {
    RsMd rsMd = new RsMd(Capability.DESCRIPTION.xmlValue);
    Urlset sourceDescription = new Urlset(rsMd);

    for (DataSet dataSet : dataSetRepository.getDataSetsWithReadAccess(user)) {
      DataSetMetaData dataSetMetaData = dataSet.getMetadata();
      String loc = rsUriHelper.uriForRsDocument(dataSetMetaData, Capability.CAPABILITYLIST);
      String descriptionUrl = rsUriHelper.uriForFilename(dataSetMetaData, DESCRIPTION_FILENAME);
      UrlItem item = new UrlItem(loc)
        .withMetadata(new RsMd(Capability.CAPABILITYLIST.xmlValue))
        .addLink(new RsLn(REL_DESCRIBED_BY, descriptionUrl)
          .withType(DESCRIPTION_TYPE));
      sourceDescription.addItem(item);
    }
    return sourceDescription;
  }

  public Urlset getCapabilityList(@Nullable User user, String ownerId, String dataSetId) {
    RsMd rsMd = new RsMd(Capability.CAPABILITYLIST.xmlValue);
    Urlset capabilityList = new Urlset(rsMd)
      .addLink(new RsLn(REL_UP, rsUriHelper.uriForWellKnownResourceSync()));

    Optional<DataSet> maybeDataSet = dataSetRepository.getDataSet(user, ownerId, dataSetId);
    if (maybeDataSet.isPresent()) {
      DataSetMetaData dataSetMetaData = maybeDataSet.get().getMetadata();
      String descriptionUrl = rsUriHelper.uriForFilename(dataSetMetaData, DESCRIPTION_FILENAME);
      capabilityList.addLink(new RsLn(REL_DESCRIBED_BY, descriptionUrl)
        .withType(DESCRIPTION_TYPE));
      String loc = rsUriHelper.uriForRsDocument(dataSetMetaData, Capability.RESOURCELIST);
      UrlItem item = new UrlItem(loc)
        .withMetadata(new RsMd(Capability.RESOURCELIST.xmlValue));
      capabilityList.addItem(item);
    }
    return capabilityList;
  }

  // Can be dismissed on dynamic creation of resourcelist
  public Optional<File> getResourceListFile(@Nullable User user, String ownerId, String dataSetId) {
    File file = null;
    Optional<DataSet> maybeDataSet = dataSetRepository.getDataSet(user, ownerId, dataSetId);
    if (maybeDataSet.isPresent()) {
      file = resourceSync.getResourceListFile(ownerId, dataSetId);
    }
    return Optional.ofNullable(file);
  }

  public Optional<CachedFile> getCachedFile(@Nullable User user, String ownerId, String dataSetId, String fileId)
    throws ResourceSyncException {
    Optional<DataSet> maybeDataSet = dataSetRepository.getDataSet(user, ownerId, dataSetId);
    if (maybeDataSet.isPresent()) {
      return resourceSync.getFile(ownerId, dataSetId, fileId); // 200 Ok, not found
    }
    
    return Optional.empty();
  }


}
