package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;

class SourceDescription {
  private final ResourceSyncXmlHelper xmlHelper;

  SourceDescription(File sourceDescriptionFile) throws ResourceSyncException {
    xmlHelper = new ResourceSyncXmlHelper(sourceDescriptionFile, this::updateMetaData);
  }

  private void updateMetaData(Node root, Document doc) {
    if (root.getChildNodes().getLength() == 0) {
      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("capability", "description");
      root.appendChild(metaData);
    }
  }

  void addCapabilityList(File capabilityListFile) throws ResourceSyncException {
    xmlHelper.addUrlElementWithCapability(capabilityListFile, "capabilitylist");
    xmlHelper.save();
  }

}
