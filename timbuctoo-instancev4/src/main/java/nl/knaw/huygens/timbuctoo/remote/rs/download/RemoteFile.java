package nl.knaw.huygens.timbuctoo.remote.rs.download;

import nl.knaw.huygens.timbuctoo.remote.rs.download.exceptions.CantRetrieveFileException;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.InputStream;

@Value.Immutable
public interface RemoteFile {
  String getUrl();

  RemoteData getData();

  String getMimeType();

  Metadata getMetadata();

  static RemoteFile create(String url, RemoteData data, String mimeType, Metadata metadata) {
    return ImmutableRemoteFile.builder()
      .data(data)
      .url(url)
      .mimeType(mimeType)
      .metadata(metadata)
      .build();
  }

  interface RemoteData {
    InputStream get() throws IOException, CantRetrieveFileException;
  }
}

