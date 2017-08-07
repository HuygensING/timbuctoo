package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;

import java.io.File;
import java.time.Instant;


/**
 * A class to handle a server side resource sync: http://www.openarchives.org/rs/resourcesync
 */
public class ResourceSync {

  private final String resourceSyncUri;
  private final FileHelper fileHelper;

  public ResourceSync(String resourceSyncUri,
                      FileHelper fileHelper) {
    this.resourceSyncUri = resourceSyncUri;
    this.fileHelper = fileHelper;
  }

  public ResourceList resourceList(String user, String dataSet) {
    return new FileSystemResourceList(getResourceListFile(user, dataSet), () -> Instant.now().toString());
  }

  private File getResourceListFile(String user, String dataSet) {
    return fileHelper.fileInDataSet(user, dataSet, "resourceList.xml");
  }

  public void addDataSet(String user, String dataSet) throws ResourceSyncException {
    File sourceDescriptionFile = fileHelper.fileInRoot("sourceDescription.xml");
    File capabilityListFile = fileHelper.fileInDataSet(user, dataSet, "capabilityList.xml");
    CapabilityList capabilityList = new CapabilityList(
      capabilityListFile,
      sourceDescriptionFile
    );
    capabilityList.addResourceList(getResourceListFile(user, dataSet));

    SourceDescription sourceDescription = new SourceDescription(sourceDescriptionFile);
    sourceDescription.addCapabilityList(capabilityListFile);


  }
}
