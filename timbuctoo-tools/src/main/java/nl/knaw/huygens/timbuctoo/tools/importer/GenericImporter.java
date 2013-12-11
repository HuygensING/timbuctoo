package nl.knaw.huygens.timbuctoo.tools.importer;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.util.Progress;

/**
 * A sub class of the GenericDataHandler, that imports the data directly into the database. 
 */
public class GenericImporter extends GenericDataHandler {

  protected final StorageManager storageManager;

  public GenericImporter(StorageManager storageManager) {
    this.storageManager = storageManager;
  }

  @Override
  protected <T extends DomainEntity> void save(Class<T> type, List<T> objects, Change change) throws IOException {
    Progress progress = new Progress();
    for (T object : objects) {
      progress.step();
      storageManager.addDomainEntity(type, object, change);
    }
    progress.done();
  }

}
