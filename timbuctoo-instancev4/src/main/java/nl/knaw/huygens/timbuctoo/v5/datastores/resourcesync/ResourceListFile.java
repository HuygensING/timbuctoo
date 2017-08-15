package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.time.Clock;
import java.util.Optional;

class ResourceListFile implements ResourceList {
  private final File resourceList;
  private final Clock clock;
  private final ResourceSyncUriHelper uriHelper;

  ResourceListFile(File resourceList, ResourceSyncUriHelper uriHelper) {
    this(resourceList, Clock.systemUTC(), uriHelper);
  }

  ResourceListFile(File resourceList, Clock clock, ResourceSyncUriHelper uriHelper) {
    this.resourceList = resourceList;
    this.clock = clock;
    this.uriHelper = uriHelper;
  }

  @Override
  public void addFile(CachedFile fileToAdd) throws ResourceSyncException {
    ResourceSyncXmlHelper xmlHelper = new ResourceSyncXmlHelper(resourceList, this::updateMetaData);

    Optional<MediaType> mimeType = fileToAdd.getMimeType();
    if (mimeType.isPresent()) {
      xmlHelper.addUrlElementWithType(uriHelper.uriForFile(fileToAdd.getFile()), mimeType.get().toString());
    } else {
      xmlHelper.addUrlElement(uriHelper.uriForFile(fileToAdd.getFile()));
    }
    xmlHelper.save();
  }

  private void updateMetaData(Node root, Document doc) {
    String currentTime = clock.instant().toString();
    if (root.getChildNodes().getLength() == 0) {
      Element metaData = doc.createElement("rs:md");
      metaData.setAttribute("at", currentTime); //ToString used ISO-8601
      metaData.setAttribute("capability", "resourcelist");
      root.appendChild(metaData);
    } else {
      root.getFirstChild().getAttributes().getNamedItem("at").setNodeValue(currentTime);
    }
  }
}
