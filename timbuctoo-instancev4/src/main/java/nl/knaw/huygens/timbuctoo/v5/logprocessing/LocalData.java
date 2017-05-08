package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Optional;

/**
 * In interface that represents a log file that is already stored locally and can be repeatedly
 * read from.
 */
public interface LocalData {
  /**
   * The worldwide unique name for this log (an iri, ending with the original filename, but does not necessarily point
   * to the file)
   */
  URI getName();

  /**
   * A reader that returns a raw rdf datafile character by character
   */
  Reader getReader() throws IOException;

  Writer getAppendingWriter() throws FileNotFoundException;

  /**
   * The mimetype of the file if it was explicitly stored when creating the LocalLog.
   *
   * <p>If this is empty you should perform mimeType autodetection based on the filename or the
   * contents.
   */
  Optional<String> getMimeType();

}
