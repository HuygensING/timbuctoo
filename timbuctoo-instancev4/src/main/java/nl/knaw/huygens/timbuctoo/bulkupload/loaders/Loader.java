package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;

import java.io.File;
import java.io.IOException;

public interface Loader {
  void loadData(File file, Importer importer) throws InvalidFileException, IOException;
}
