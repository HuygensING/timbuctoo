package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;

import java.io.IOException;

public interface Loader {
  void loadData(byte[] source, Importer importer) throws InvalidFileException,
    IOException;
}
