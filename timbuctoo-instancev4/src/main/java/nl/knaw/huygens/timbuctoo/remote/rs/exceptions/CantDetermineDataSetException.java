package nl.knaw.huygens.timbuctoo.remote.rs.exceptions;

import nl.knaw.huygens.timbuctoo.remote.rs.download.RemoteFile;

import java.util.List;
import java.util.stream.Collectors;

public class CantDetermineDataSetException extends Exception {
  private List<RemoteFile> remoteFiles;

  public CantDetermineDataSetException(List<RemoteFile> remoteFiles) {
    super("Can not determine dataset file. Please request again with dataset file name specified. The resource files" +
        "available are: \n" +
      remoteFiles.stream()
        .map(RemoteFile::getUrl)
        .collect(Collectors.joining(", \n")));

    this.remoteFiles = remoteFiles;
  }

  public List<RemoteFile> getRemoteFiles() {
    return remoteFiles;
  }
}
