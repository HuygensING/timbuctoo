package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;

class FileSystemResourceList implements ResourceList {
  private final File resourceList;
  private final ResourceSyncDateFormatter resourceSyncDateFormatter;
  private final ResourceSyncUriHelper uriHelper;

  public FileSystemResourceList(File resourceList, ResourceSyncDateFormatter resourceSyncDateFormatter,
                                ResourceSyncUriHelper uriHelper) {
    this.resourceList = resourceList;
    this.resourceSyncDateFormatter = resourceSyncDateFormatter;
    this.uriHelper = uriHelper;
  }

  @Override
  public void addFile(CachedFile fileToAdd) throws ResourceSyncException {
    ResourceSyncXmlHelper xmlHelper = new ResourceSyncXmlHelper(resourceList, this::updateMetaData);
    xmlHelper.addUrlElement(uriHelper.uriForFile(fileToAdd.getFile()));
    xmlHelper.save();
  }

  private void updateMetaData(Node root, Document doc) {
    if (root.getChildNodes().getLength() == 0) {
      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("at", resourceSyncDateFormatter.now());
      metaData.setAttribute("capability", "resourcelist");
      root.appendChild(metaData);
    } else {
      root.getFirstChild().getAttributes().getNamedItem("at").setNodeValue(resourceSyncDateFormatter.now());
    }
  }
}
