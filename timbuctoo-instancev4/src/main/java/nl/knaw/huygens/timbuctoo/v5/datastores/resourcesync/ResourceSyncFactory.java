package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.DataSetPathHelper;

public class ResourceSyncFactory {
  private final String resourceSyncUri;

  @JsonCreator
  public ResourceSyncFactory(@JsonProperty("resourceSyncUri") String resourceSyncUri) {
    this.resourceSyncUri = resourceSyncUri;
  }

  public ResourceSync createResourceSync(DataSetPathHelper dataSetPathHelper) {
    return new ResourceSync(resourceSyncUri, dataSetPathHelper);
  }
}
