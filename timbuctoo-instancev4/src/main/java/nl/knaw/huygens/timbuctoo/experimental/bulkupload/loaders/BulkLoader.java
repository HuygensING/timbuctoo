package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;

public interface BulkLoader<T> {
  void loadWorkbookAndMarkErrors(T wb, Importer importer);
}
