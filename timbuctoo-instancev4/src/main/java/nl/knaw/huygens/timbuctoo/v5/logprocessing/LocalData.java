package nl.knaw.huygens.timbuctoo.v5.logprocessing;

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
   * The timbuctoo-wide unique address for the sequence of bytes in this file.
   *
   * <p>Different timbuctoo instances might use the same contentAddress for different files.
   */
  String getContentAddress();

  /**
   * The URI under which this log was originally provided. This is usually the location from where the log was retrieved
   * but that is not a technical requirement. If you upload a local file you might name it file://... or invent your own
   * URI.
   *
   * <p>Systems might use the file's uri to implement usability features around duplicate files. Technical file
   * deduplication should be done using the contentAddress
   */
  URI getUri();

  /**
   * A reader that returns a raw rdf datafile character by character
   */
  Reader getReader() throws IOException;

  /**
   * A writer that appends the characters to the rdf file
   */
  Writer getAppendingWriter() throws IOException;

  /**
   * The mimetype of the file if it was explicitly stored when creating the LocalLog.
   *
   * <p>If this is empty you should perform mimeType autodetection based on the uri or the contents.
   */
  Optional<String> getMimeType();

}
