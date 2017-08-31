package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.time.Clock;

class ResourceListFile implements ResourceList {
  private final File resourceList;
  private final Clock clock;
  private final ResourceSyncUriHelper uriHelper;
  private final ResourceSyncXmlHelper xmlHelper;

  ResourceListFile(File resourceList, ResourceSyncUriHelper uriHelper) throws ResourceSyncException {
    this(resourceList, Clock.systemUTC(), uriHelper);
  }

  ResourceListFile(File resourceList, Clock clock, ResourceSyncUriHelper uriHelper) throws ResourceSyncException {
    this.resourceList = resourceList;
    this.clock = clock;
    this.uriHelper = uriHelper;
    this.xmlHelper = new ResourceSyncXmlHelper(resourceList);
    xmlHelper.getOrCreateMetadataNode().setAttribute("capability", "resourcelist");
  }

  @Override
  public void addFile(CachedFile fileToAdd) throws ResourceSyncException {
    MediaType mimeType = fileToAdd.getMimeType();
    xmlHelper.addUrlElementWithType(uriHelper.uriForFile(fileToAdd.getFile()), mimeType.toString());
    xmlHelper.getOrCreateMetadataNode().setAttribute("at", clock.instant().toString()); //ToString used ISO-8601
    xmlHelper.save();
  }

}
