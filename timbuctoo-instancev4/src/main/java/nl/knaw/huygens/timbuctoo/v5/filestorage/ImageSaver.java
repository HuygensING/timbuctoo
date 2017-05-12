package nl.knaw.huygens.timbuctoo.v5.filestorage;

import java.io.InputStream;
import java.net.URI;

public interface ImageSaver {
  URI store(InputStream rdfInputStream);
}
