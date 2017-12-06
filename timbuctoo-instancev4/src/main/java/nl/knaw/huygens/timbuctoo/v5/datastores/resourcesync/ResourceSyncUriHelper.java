package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ResourceSyncEndpoint;
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;

import java.io.File;

import static javax.ws.rs.core.UriBuilder.fromResource;
import static nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync.BASE_URI_PLACE_HOLDER;

class ResourceSyncUriHelper {
  private static final String RESOURCE_SYNC_PATH = fromResource(ResourceSyncEndpoint.class).build().getPath();
  private final FileHelper fileHelper;

  ResourceSyncUriHelper(FileHelper fileHelper) {
    this.fileHelper = fileHelper;
  }

  String uriForFile(File file) {

    return String.format("%s/%s/%s", BASE_URI_PLACE_HOLDER, RESOURCE_SYNC_PATH, fileHelper.getRelativePath(file));
  }
}
