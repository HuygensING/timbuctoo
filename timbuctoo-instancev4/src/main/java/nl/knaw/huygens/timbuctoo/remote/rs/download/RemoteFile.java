package nl.knaw.huygens.timbuctoo.remote.rs.download;

import org.immutables.value.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

@Value.Immutable
public interface RemoteFile {
  String getUrl();

  RemoteData getData();

  String getMimeType();

  static RemoteFile create(String url, RemoteData data, String mimeType) {
    return ImmutableRemoteFile.builder()
      .data(data)
      .url(url)
      .mimeType(mimeType)
      .build();
  }

  interface RemoteData {
    InputStream get() throws IOException;
  }
}

