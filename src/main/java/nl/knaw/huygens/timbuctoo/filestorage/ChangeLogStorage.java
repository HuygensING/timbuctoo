package nl.knaw.huygens.timbuctoo.filestorage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

public interface ChangeLogStorage {
  /**
   * Stores the changelog somewhere safe
   * @param version The changelog version
   * @return an output stream that can be used to write the changelog
   */
  OutputStream getChangeLogOutputStream(int version) throws IOException;

  /**
   * Returns a previously stored changelog
   *
   * @param version the version of the changelog
   * @return A Reader for the changelog that was stored
   */
  Optional<File> getChangeLog(int version);

  void clear() throws IOException;
}
