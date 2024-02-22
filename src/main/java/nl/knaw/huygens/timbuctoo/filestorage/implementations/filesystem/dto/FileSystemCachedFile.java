package nl.knaw.huygens.timbuctoo.filestorage.implementations.filesystem.dto;

import nl.knaw.huygens.timbuctoo.filestorage.dto.CachedFile;

import javax.ws.rs.core.MediaType;
import java.io.File;

public record FileSystemCachedFile(MediaType mimeType, String name, File file) implements CachedFile {
  @Override
  public void close() throws Exception {
  }
}
