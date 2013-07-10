package nl.knaw.huygens.repository.importer.database;

import java.io.IOException;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.StorageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Posts data to a local repository.
 */
public class LocalDataPoster implements DataPoster {

  private static final Logger LOG = LoggerFactory.getLogger(LocalDataPoster.class);

  private final StorageManager storageManager;

  public LocalDataPoster(StorageManager manager) {
    storageManager = manager;
  }

  @Override
  public <T extends Document> T getDocument(Class<T> type, String id) {
    return storageManager.getDocument(type, id);
  }

  @Override
  public <T extends Document> String addDocument(Class<T> type, T document, boolean isComplete) {
    try {
      storageManager.addDocument(type, document, isComplete);
      return document.getId();
    } catch (IOException e) {
      LOG.error("Failed to modify {}; {}", document.getDisplayName(), e.getMessage());
      return null;
    }
  }

  @Override
  public <T extends Document> T modDocument(Class<T> type, T document) {
    try {
      storageManager.modifyDocument(type, document);
      return document;
    } catch (IOException e) {
      LOG.error("Failed to modify {}; {}", document.getDisplayName(), e.getMessage());
      return null;
    }
  }

}
