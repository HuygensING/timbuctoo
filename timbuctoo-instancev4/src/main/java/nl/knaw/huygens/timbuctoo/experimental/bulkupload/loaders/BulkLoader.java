package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;

public interface BulkLoader<T, U> {
  U loadData(T wb, Importer importer) throws InvalidExcelFileException;
}
