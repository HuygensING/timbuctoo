package nl.knaw.huygens.timbuctoo.remote.rs.download;

import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader.RemoteFilesList;
import nl.knaw.huygens.timbuctoo.remote.rs.exceptions.CantDetermineDataSetException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterRemoteFilesForImport {
  private ResourceSyncFileLoader resourceSyncFileLoader;

  public FilterRemoteFilesForImport(ResourceSyncFileLoader resourceSyncFileLoader) {
    this.resourceSyncFileLoader = resourceSyncFileLoader;
  }


  public List<RemoteFile> filter(String capabilityListUri) throws CantDetermineDataSetException {
    try {
      RemoteFilesList remoteFilesList =
        resourceSyncFileLoader.getRemoteFilesList(capabilityListUri);

      if (!remoteFilesList.getChangeList().isEmpty()) {
        return remoteFilesList.getChangeList();
      }

      List<RemoteFile> resources = new ArrayList<>();

      for (RemoteFile remoteFile : remoteFilesList.getResourceList()) {
        if (remoteFile.getMetadata().isDataset()) {
          resources.add(remoteFile);
          return resources;
        }
      }

      if (remoteFilesList.getResourceList().size() == 1) {
        resources.add(remoteFilesList.getResourceList().get(0));
      } else {
        throw new CantDetermineDataSetException(remoteFilesList.getResourceList());
      }

      return resources;

    } catch (IOException e) {
      return Collections.emptyList();
    }

  }

  public List<RemoteFile> filter(String capabilityListUri, String userSpecifiedDataSet) {
    try {
      RemoteFilesList remoteFilesList =
        resourceSyncFileLoader.getRemoteFilesList(capabilityListUri);

      List<RemoteFile> resources = new ArrayList<>();

      for (RemoteFile remoteFile : remoteFilesList.getResourceList()) {
        if (remoteFile.getUrl().equals(userSpecifiedDataSet)) {
          resources.add(remoteFile);
          break;
        }
      }

      return resources;

    } catch (IOException e) {
      return Collections.emptyList();
    }
  }
}
