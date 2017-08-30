package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import java.io.File;

class SourceDescriptionFile {
  private final ResourceSyncUriHelper uriHelper;
  private final ResourceSyncXmlHelper xmlHelper;

  SourceDescriptionFile(File sourceDescriptionFile,
                        ResourceSyncUriHelper uriHelper) throws ResourceSyncException {
    this.uriHelper = uriHelper;
    this.xmlHelper = new ResourceSyncXmlHelper(sourceDescriptionFile);
    xmlHelper.getOrCreateMetadataNode().setAttribute("capability", "description");
  }

  void addCapabilityList(File capabilityListFile) throws ResourceSyncException {
    xmlHelper.addUrlElementWithCapability(uriHelper.uriForFile(capabilityListFile), "capabilitylist");
    xmlHelper.save();
  }

  void removeCapabilityList(File capabilityListFile) throws ResourceSyncException {
    xmlHelper.removeUrlElement(uriHelper.uriForFile(capabilityListFile));
    xmlHelper.save();
  }
}
