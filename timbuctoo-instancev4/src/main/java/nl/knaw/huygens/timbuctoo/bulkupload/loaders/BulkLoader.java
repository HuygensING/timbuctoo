package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;

public interface BulkLoader<T, U> {
  U loadData(T wb, Importer importer) throws InvalidExcelFileException;
}
