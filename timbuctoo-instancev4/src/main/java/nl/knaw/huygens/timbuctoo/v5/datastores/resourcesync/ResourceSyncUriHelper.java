package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;

import javax.ws.rs.core.UriBuilder;
import java.io.File;

class ResourceSyncUriHelper {
  private final String resourceSyncUri;
  private final FileHelper fileHelper;

  ResourceSyncUriHelper(String resourceSyncUri, FileHelper fileHelper) {
    this.resourceSyncUri = resourceSyncUri;
    this.fileHelper = fileHelper;
  }

  String uriForFile(File file) {
    return UriBuilder.fromUri(resourceSyncUri).path(fileHelper.getRelativePath(file)).build().toString();
  }
}
