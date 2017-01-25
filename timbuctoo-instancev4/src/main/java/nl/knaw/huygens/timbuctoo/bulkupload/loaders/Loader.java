package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A Loader applies an Importer to the contents of a File.
 * Specific Loader implementations know how to parse a file format and call the
 * required Importer methods.
 */
public interface Loader {
  void loadData(List<Tuple<String, File>> files, Importer importer) throws InvalidFileException, IOException;
}
