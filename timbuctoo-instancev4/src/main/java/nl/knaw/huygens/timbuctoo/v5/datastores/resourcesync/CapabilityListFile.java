package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import java.io.File;

class CapabilityListFile {

  private final ResourceSyncXmlHelper xmlHelper;
  private final ResourceSyncUriHelper uriHelper;

  CapabilityListFile(File file, File sourceDescription, ResourceSyncUriHelper uriHelper) throws ResourceSyncException {
    this.uriHelper = uriHelper;
    this.xmlHelper = new ResourceSyncXmlHelper(file);
    xmlHelper.getOrCreateMetadataNode().setAttribute("capability", "capabilitylist");
    xmlHelper.createOrUpdateUplink(uriHelper.uriForFile(sourceDescription));
  }

  void addResourceList(File resourceListFile) throws ResourceSyncException {
    xmlHelper.addUrlElementWithCapability(uriHelper.uriForFile(resourceListFile), "resourcelist");
    xmlHelper.save();
  }

}
