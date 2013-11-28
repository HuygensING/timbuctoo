package nl.knaw.huygens.timbuctoo.tools.importer;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.util.Progress;

/**
 * A sub class of the GenericDataHandler, that imports the data directly into the database. 
 */
public class GenericImporter extends GenericDataHandler {

  protected StorageManager storageManager;

  public GenericImporter(StorageManager storageManager) {
    this.storageManager = storageManager;
  }

  @Override
  protected <T extends Entity> void save(Class<T> type, List<T> objects) throws IOException {
    Progress progress = new Progress();
    for (T object : objects) {
      progress.step();
      storageManager.addEntity(type, object);
    }
    progress.done();
  }

}
