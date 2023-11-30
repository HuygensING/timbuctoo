package nl.knaw.huygens.timbuctoo.filestorage.dto;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * In interface that represents a log file that is already stored locally and can be repeatedly
 * read from.
 */
public interface CachedLog extends CachedFile {
  Charset getCharset();

  Reader getReader() throws IOException;
}
