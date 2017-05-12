package nl.knaw.huygens.timbuctoo.v5.filestorage;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalData;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public interface FileSaver {
  LocalData store(Optional<String> mediatyOpe, Optional<Charset> charset, InputStream rdfInputStream);
}
