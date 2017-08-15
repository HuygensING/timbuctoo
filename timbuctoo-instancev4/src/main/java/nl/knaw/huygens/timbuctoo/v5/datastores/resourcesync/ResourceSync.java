package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;

import java.io.File;


/**
 * A class to handle a server side resource sync: http://www.openarchives.org/rs/resourcesync
 */
public class ResourceSync {

  private final ResourceSyncUriHelper uriHelper;
  private final FileHelper fileHelper;

  public ResourceSync(ResourceSyncUriHelper uriHelper, FileHelper fileHelper) {
    this.uriHelper = uriHelper;
    this.fileHelper = fileHelper;
  }

  public ResourceList resourceList(String user, String dataSet) {
    return new ResourceListFile(getResourceListFile(user, dataSet), uriHelper);
  }

  private File getResourceListFile(String user, String dataSet) {
    return fileHelper.fileInDataSet(user, dataSet, "resourceList.xml");
  }

  public void addDataSet(String user, String dataSet) throws ResourceSyncException {
    File sourceDescriptionFile = fileHelper.fileInRoot("sourceDescription.xml");
    File capabilityListFile = fileHelper.fileInDataSet(user, dataSet, "capabilityList.xml");

    CapabilityListFile capabilityList = new CapabilityListFile(capabilityListFile, sourceDescriptionFile, uriHelper);
    capabilityList.addResourceList(getResourceListFile(user, dataSet));

    SourceDescriptionFile sourceDescription = new SourceDescriptionFile(sourceDescriptionFile, uriHelper);
    sourceDescription.addCapabilityList(capabilityListFile);
  }

  public void removeDataSet(String user, String dataSet) throws ResourceSyncException {
    File sourceDescriptionFile = fileHelper.fileInRoot("sourceDescription.xml");
    File capabilityListFile = fileHelper.fileInDataSet(user, dataSet, "capabilityList.xml");

    SourceDescriptionFile sourceDescription = new SourceDescriptionFile(sourceDescriptionFile, uriHelper);
    sourceDescription.removeCapabilityList(capabilityListFile);
  }
}
