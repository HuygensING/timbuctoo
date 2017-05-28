package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

public class FileSystemCachedLog implements CachedLog {

  private final Optional<MediaType> mimeType;
  private final Optional<Charset> charset;
  private final URI name;
  private final File file;

  public FileSystemCachedLog(Optional<MediaType> mimeType, Optional<Charset> charset, URI name, File file) {
    this.mimeType = mimeType;
    this.charset = charset;
    this.name = name;
    this.file = file;
  }

  @Override
  public URI getName() {
    return name;
  }

  @Override
  public Reader getReader() throws IOException {
    return null;
  }

  @Override
  public Optional<MediaType> getMimeType() {
    return mimeType;
  }
}
