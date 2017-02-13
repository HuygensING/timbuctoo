package nl.knaw.huygens.timbuctoo.remote.rs.download;

import org.immutables.value.Value;

import java.io.InputStream;

@Value.Immutable
public interface RemoteFile {
  String getUrl();

  InputStream getData();

  String getMimeType();

  static RemoteFile create(String url, InputStream data, Metadata metadata) {
    return ImmutableRemoteFile.builder()
      .data(data)
      .url(url)
      .mimeType(metadata.getMimeType())
      .build();
  }
}
