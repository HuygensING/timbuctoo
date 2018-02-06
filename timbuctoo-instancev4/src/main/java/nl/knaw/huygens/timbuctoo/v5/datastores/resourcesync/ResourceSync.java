package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;


/**
 * A class to handle a server side resource sync: http://www.openarchives.org/rs/resourcesync
 */
public class ResourceSync {

  public static final String BASE_URI_PLACE_HOLDER = "${TIMBUCTOO_BASE_URI}";
  private final ResourceSyncUriHelper uriHelper;
  private final FileHelper fileHelper;
  private final FileStorageFactory fileStorageFactory;

  public ResourceSync(FileHelper fileHelper,
                      FileStorageFactory fileStorageFactory) {
    this.uriHelper = new ResourceSyncUriHelper(fileHelper);
    this.fileHelper = fileHelper;
    this.fileStorageFactory = fileStorageFactory;
  }

  public ResourceList resourceList(String user, String dataSet) throws ResourceSyncException {
    File sourceDescriptionFile = getSourceDescriptionFile();
    File capabilityListFile = getCapabilityListFile(user, dataSet);

    CapabilityListFile capabilityList = new CapabilityListFile(capabilityListFile, sourceDescriptionFile, uriHelper);
    File resourceListFile = getResourceListFile(user, dataSet);
    capabilityList.addResourceList(resourceListFile);

    SourceDescriptionFile sourceDescription = new SourceDescriptionFile(sourceDescriptionFile, uriHelper);
    sourceDescription.addCapabilityList(capabilityListFile, getDataSetDescriptionFile(user, dataSet));
    return new ResourceListFile(resourceListFile, uriHelper);
  }

  public void removeDataSet(String user, String dataSet) throws ResourceSyncException {
    File sourceDescriptionFile = getSourceDescriptionFile();
    File capabilityListFile = getCapabilityListFile(user, dataSet);

    SourceDescriptionFile sourceDescription = new SourceDescriptionFile(sourceDescriptionFile, uriHelper);
    sourceDescription.removeCapabilityList(capabilityListFile);
  }

  public File getSourceDescriptionFile() {
    return fileHelper.fileInRoot("sourceDescription.xml");
  }

  public File getCapabilityListFile(String owner, String dataSetName) {
    return fileHelper.fileInDataSet(owner, dataSetName, "capabilityList.xml");
  }

  public File getResourceListFile(String user, String dataSet) {
    return fileHelper.fileInDataSet(user, dataSet, "resourceList.xml");
  }

  public Optional<CachedFile> getFile(String owner, String dataSet, String fileId) throws ResourceSyncException {
    try {
      // DANGER! localhost../v5/resourcesync/foo/bar/files/hack-a-directory
      return fileStorageFactory.makeFileStorage(owner, dataSet).getFile(fileId);
    } catch (IOException e) {
      throw new ResourceSyncException(e);
    }
  }

  public File getDataSetDescriptionFile(String user, String dataSet) {
    return fileHelper.fileInDataSet(user, dataSet, "description.xml");
  }
}
