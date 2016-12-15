package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.core.dto.CreateCollection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;

public class RdfImportSession {

  static RdfImportSession cleanImportSession(String vreName, DataStoreOperations dataStoreOperations) {
    Vre vre = dataStoreOperations.ensureVreExists(vreName);
    dataStoreOperations.addCollectionToVre(vre, CreateCollection.defaultCollection());
    dataStoreOperations.clearMappingErrors(vre);
    dataStoreOperations.removeCollectionsAndEntities(vre);

    return new RdfImportSession();
  }

  public void close() {
    // No implementation needed yet
  }

  public void success() {
    // No implementation needed yet
  }

  public void rollback() {
    // No implementation needed yet
  }
}
