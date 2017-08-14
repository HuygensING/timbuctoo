package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;

import java.io.File;
import java.io.IOException;


/**
 * A class to handle a server side resource sync: http://www.openarchives.org/rs/resourcesync
 */
public class ResourceSync {

  private final ResourceSyncUriHelper uriHelper;
  private final FileHelper fileHelper;
  private final FileStorageFactory fileStorageFactory;

  public ResourceSync(ResourceSyncUriHelper uriHelper,
                      FileHelper fileHelper,
                      FileStorageFactory fileStorageFactory) {
    this.uriHelper = uriHelper;
    this.fileHelper = fileHelper;
    this.fileStorageFactory = fileStorageFactory;
  }

  public ResourceList resourceList(String user, String dataSet) {
    return new ResourceListFile(getResourceListFile(user, dataSet), uriHelper);
  }

  public void addDataSet(String user, String dataSet) throws ResourceSyncException {
    File sourceDescriptionFile = getSourceDescriptionFile();
    File capabilityListFile = getCapabilityListFile(user, dataSet);

    CapabilityListFile capabilityList = new CapabilityListFile(capabilityListFile, sourceDescriptionFile, uriHelper);
    capabilityList.addResourceList(getResourceListFile(user, dataSet));

    SourceDescriptionFile sourceDescription = new SourceDescriptionFile(sourceDescriptionFile, uriHelper);
    sourceDescription.addCapabilityList(capabilityListFile);
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

  public CachedFile getFile(String owner, String dataSet, String fileId) throws ResourceSyncException {
    try {
      return fileStorageFactory.makeFileStorage(owner, dataSet).getFile(fileId);
    } catch (IOException e) {
      throw new ResourceSyncException(e);
    }
  }
}
