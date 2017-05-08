package nl.knaw.huygens.timbuctoo.remote.rs.download;

import org.immutables.value.Value;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

@Value.Immutable
public interface RemoteFile {
  URI getUrl();

  InputStream getData();

  Optional<String> getMimeType();

  static RemoteFile create(URI url, InputStream data, String mimeType) {
    return ImmutableRemoteFile.builder()
      .data(data)
      .url(url)
      .mimeType(Optional.ofNullable(mimeType))
      .build();
  }
}
