package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;

public class ResourceSyncFactory {
  private final String resourceSyncUri;

  @JsonCreator
  public ResourceSyncFactory(@JsonProperty("resourceSyncUri") String resourceSyncUri) {
    this.resourceSyncUri = resourceSyncUri;
  }

  public ResourceSync createResourceSync(FileHelper fileHelper,
                                         FileStorageFactory fileStorageFactory) {
    return new ResourceSync(new ResourceSyncUriHelper(resourceSyncUri, fileHelper), fileHelper, fileStorageFactory);
  }
}
