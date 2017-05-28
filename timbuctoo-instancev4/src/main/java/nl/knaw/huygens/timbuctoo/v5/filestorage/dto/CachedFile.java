package nl.knaw.huygens.timbuctoo.v5.filestorage.dto;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface CachedFile {
  String getName();

  InputStream getStream() throws IOException;

  Optional<MediaType> getMimeType();
}
