package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.DataSetPathHelper;

import java.io.File;
import java.time.Instant;


/**
 * A class to handle a server side resource sync: http://www.openarchives.org/rs/resourcesync
 */
public class ResourceSync {

  private final String resourceSyncUri;
  private final DataSetPathHelper dataSetPathHelper;

  public ResourceSync(String resourceSyncUri,
                      DataSetPathHelper dataSetPathHelper) {
    this.resourceSyncUri = resourceSyncUri;
    this.dataSetPathHelper = dataSetPathHelper;
  }

  public ResourceList resourceList(String user, String dataSet) {
    // TODO add capability list to source description
    return new FileSystemResourceList(
      new File(dataSetPathHelper.dataSetPath(user, dataSet), "resourceList.xml"),
      () -> Instant.now().toString()
    );
  }

  private File getResourceListFile(String user, String dataSet) {
    return dataSetPathHelper.fileInDataSet(user, dataSet, "resourceList.xml");
  }

  public void addDataSet(String user, String dataSet) throws ResourceSyncException {
    new CapabilityList(
      dataSetPathHelper.fileInDataSet(user, dataSet, "capabilityList.xml"),
      new File("sourceDescription.xml")
    ).addResourceList(getResourceListFile(user, dataSet));
  }
}
