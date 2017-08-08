package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;

import static nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncXmlHelper.setUplink;

class CapabilityList {

  private final ResourceSyncXmlHelper xmlHelper;
  private final ResourceSyncUriHelper uriHelper;

  CapabilityList(File file, File sourceDescription,
                 ResourceSyncUriHelper uriHelper) throws ResourceSyncException {
    this.uriHelper = uriHelper;
    xmlHelper = new ResourceSyncXmlHelper(file, (root, doc) -> updateMetaData(root, doc, sourceDescription));

  }

  private void updateMetaData(Node root, Document doc, File sourceDescription) {
    if (root.getChildNodes().getLength() == 0) {
      setUplink(root, doc, uriHelper.uriForFile(sourceDescription));

      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("capability", "capabilitylist");
      root.appendChild(metaData);
    }
  }

  void addResourceList(File resourceListFile) throws ResourceSyncException {
    xmlHelper.addUrlElementWithCapability(uriHelper.uriForFile(resourceListFile), "resourcelist");
    xmlHelper.save();

  }

}
