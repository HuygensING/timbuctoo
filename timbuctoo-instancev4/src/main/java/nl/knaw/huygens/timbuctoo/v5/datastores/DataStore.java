package nl.knaw.huygens.timbuctoo.v5.datastores;

import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatus;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.DataProcessor;

public interface DataStore<T> extends DataProcessor<T>, AutoCloseable {
  StoreStatus getStatus();
}
