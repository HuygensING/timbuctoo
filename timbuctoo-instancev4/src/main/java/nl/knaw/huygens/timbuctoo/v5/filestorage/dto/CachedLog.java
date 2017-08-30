package nl.knaw.huygens.timbuctoo.v5.filestorage.dto;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.Optional;

/**
 * In interface that represents a log file that is already stored locally and can be repeatedly
 * read from.
 */
public interface CachedLog extends CachedFile{
  Reader getReader() throws IOException;

  Optional<MediaType> getMimeType();
}
