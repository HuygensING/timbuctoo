package nl.knaw.huygens.timbuctoo.v5.filestorage.dto;

import javax.ws.rs.core.MediaType;
import java.io.File;

public interface CachedFile extends AutoCloseable {
  String getName();

  File getFile();

  MediaType getMimeType();
}
