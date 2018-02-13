package nl.knaw.huygens.timbuctoo.v5.datastorage;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.datastorage.exceptions.DataStorageSaveException;

public interface DataSetStorage {
  void saveMetaData(DataSetMetaData metaData) throws DataStorageSaveException;
}
