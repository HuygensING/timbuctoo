package nl.knaw.huygens.timbuctoo.filestorage.dto;

import javax.ws.rs.core.MediaType;
import java.io.File;

public interface CachedFile extends AutoCloseable {
  /**
   * @return the original name of the file
   */
  String name();

  File file();

  MediaType mimeType();

  boolean replaceData();

  boolean isInverse();
}
