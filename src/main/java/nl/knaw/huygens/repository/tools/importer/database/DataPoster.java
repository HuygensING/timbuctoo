package nl.knaw.huygens.repository.tools.importer.database;

import nl.knaw.huygens.repository.model.Document;

/**
 * Abstracts posting of data to the repository.
 * Could be local or remote.
 */
public interface DataPoster {

  <T extends Document> T getDocument(Class<T> type, String id);

  <T extends Document> String addDocument(Class<T> type, T document, boolean isComplete);

  <T extends Document> T modDocument(Class<T> type, T document);

}
