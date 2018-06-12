package nl.knaw.huygens.timbuctoo.remote.rs.exceptions;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import nl.knaw.huygens.timbuctoo.remote.rs.download.RemoteFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CantDetermineDataSetException extends Exception implements GraphQLError {
  List<RemoteFile> remoteFiles;

  public CantDetermineDataSetException(List<RemoteFile> remoteFiles) {
    this.remoteFiles = remoteFiles;
//    super("Can not determine dataset file. Please request again with dataset file name specified. The resource files"
//       + "available are: \n" +
//      remoteFiles.stream()
//        .map(RemoteFile::getUrl)
//        .collect(Collectors.joining(", \n")));
  }

  @Override
  public List<SourceLocation> getLocations() {
    return null;
  }

  @Override
  public ErrorType getErrorType() {
    return ErrorType.DataFetchingException;
  }

  @Override
  public Map<String, Object> getExtensions() {
    Map<String,Object> availableFiles = new LinkedHashMap<>();

    List<String> remoteFilesUrls = remoteFiles.stream().map(RemoteFile::getUrl).collect(Collectors.toList());

    availableFiles.put("fileUrls", remoteFilesUrls);

    return availableFiles;
  }
}
