package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;

import java.util.function.Consumer;

public interface BulkLoader<T> {
  void loadData(T source, Importer importer, Consumer<String> statusUpdate) throws InvalidExcelFileException;
}
