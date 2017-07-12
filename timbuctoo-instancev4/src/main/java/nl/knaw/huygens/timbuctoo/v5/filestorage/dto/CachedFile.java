package nl.knaw.huygens.timbuctoo.v5.filestorage.dto;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Optional;

public interface CachedFile extends AutoCloseable {
  String getName();

  File getFile();

  Optional<MediaType> getMimeType();
}
