package nl.knaw.huygens.timbuctoo.v5.rdfreader;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;

import java.io.Reader;
import java.net.URI;
import java.util.Optional;

public interface RdfParser {
  void loadFile(URI fileUri, Optional<String> mimeType, Reader data, QuadHandler quadHandler) throws
    LogProcessingFailedException;
}
