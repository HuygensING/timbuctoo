package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;

import java.util.function.Consumer;

public interface BulkLoader {
  void loadData(byte[] source, Importer importer, Consumer<String> statusUpdate) throws InvalidFileException;
}
