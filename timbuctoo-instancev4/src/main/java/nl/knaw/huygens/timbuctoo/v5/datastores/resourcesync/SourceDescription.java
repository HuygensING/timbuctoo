package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;

class SourceDescription {
  private final ResourceSyncXmlHelper xmlHelper;
  private final ResourceSyncUriHelper uriHelper;

  SourceDescription(File sourceDescriptionFile,
                    ResourceSyncUriHelper uriHelper) throws ResourceSyncException {
    xmlHelper = new ResourceSyncXmlHelper(sourceDescriptionFile, this::updateMetaData);
    this.uriHelper = uriHelper;
  }

  private void updateMetaData(Node root, Document doc) {
    if (root.getChildNodes().getLength() == 0) {
      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("capability", "description");
      root.appendChild(metaData);
    }
  }

  void addCapabilityList(File capabilityListFile) throws ResourceSyncException {
    xmlHelper.addUrlElementWithCapability(uriHelper.uriForFile(capabilityListFile), "capabilitylist");
    xmlHelper.save();
  }

}
