package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;

public interface ResourceList {
  void addFile(CachedFile fileToAdd);
}
