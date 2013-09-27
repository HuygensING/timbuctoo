package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.model.Relation;

public interface VariationStorage extends BasicStorage {

  <T extends Document> List<T> getAllVariations(Class<T> type, String id) throws IOException;

  /**
   * Get the given variation of a document.
   * @param type
   * @param id
   * @param variation
   * @return
   * @throws IOException 
   */
  <T extends DomainDocument> T getVariation(Class<T> type, String id, String variation) throws IOException;

  <T extends DomainDocument> T getRevision(Class<T> type, String id, int revisionId) throws IOException;

  /**
   * Counts the number of stored relations with non-null fields
   * as in the specified {@Relation} instance.
   */
  int countRelations(Relation relation);

  /**
   * Returns all the ids of objects of type <T>, that are not persisted.
   * @param type
   * @return
   */
  <T extends DomainDocument> Collection<String> getAllIdsWithOutPIDOfType(Class<T> type);

  /**
   * Permanently removes the objects from the database.
   * 
   * @param type 
   * @param ids
   */
  <T extends DomainDocument> void removePermanently(Class<T> type, Collection<String> ids);
}
