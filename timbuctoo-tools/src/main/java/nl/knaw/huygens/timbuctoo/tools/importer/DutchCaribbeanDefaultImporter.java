package nl.knaw.huygens.timbuctoo.tools.importer;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

/**
 * A class that contains the base functionality used in both the {@code DutchCaribbeanImporter} 
 * as the now removed {@code AtlantischeGidsImporter}.
 * Those importers use(d) the data from the AtlantischeGids project.
 */
public abstract class DutchCaribbeanDefaultImporter extends DefaultImporter {

  /** File with {@code RelationType} definitions; must be present on classpath. */
  private static final String RELATION_TYPE_DEFS = "relationtype-defs.txt";

  private String prevMessage;
  private int errors;

  public DutchCaribbeanDefaultImporter(TypeRegistry registry, StorageManager storageManager, RelationManager relationManager, IndexManager indexManager) {
    super(registry, storageManager, indexManager);
    prevMessage = "";
    errors = 0;
    setup(relationManager);
  }

  protected void setup(RelationManager relationManager) {
    if (relationManager != null) {
      relationManager.importRelationTypes(RELATION_TYPE_DEFS);
    }
  }

  // --- error handling ------------------------------------------------

  protected void handleError(String format, Object... args) {
    errors++;
    String message = String.format(format, args);
    if (!message.equals(prevMessage)) {
      System.out.print("## ");
      System.out.printf(message);
      System.out.println();
      prevMessage = message;
    }
  }

  protected void displayErrorSummary() {
    if (errors > 0) {
      System.out.printf("%n## Error count = %d%n", errors);
    }
  }

  // --- storage -------------------------------------------------------

  protected <T extends Entity> T getEntity(Class<T> type, String id) {
    return storageManager.getEntity(type, id);
  }

  protected <T extends Entity> String addEntity(Class<T> type, T entity) {
    try {
      storageManager.addEntity(type, entity);
      return entity.getId();
    } catch (IOException e) {
      handleError("Failed to add %s; %s", entity.getDisplayName(), e.getMessage());
      return null;
    }
  }

  protected <T extends Entity> T modEntity(Class<T> type, T entity) {
    try {
      storageManager.modifyEntity(type, entity);
      return entity;
    } catch (IOException e) {
      handleError("Failed to modify %s; %s", entity.getDisplayName(), e.getMessage());
      return null;
    }
  }

  // --- indexing ------------------------------------------------------

  protected <T extends DomainEntity> void indexEntities(Class<T> type) throws IndexException {
    System.out.println(".. " + type.getSimpleName());
    StorageIterator<T> iterator = null;
    try {
      iterator = storageManager.getAll(type);
      while (iterator.hasNext()) {
        T entity = iterator.next();
        indexManager.addEntity(type, entity.getId());
      }
    } finally {
      if (iterator != null) {
        iterator.close();
      }
    }
  }

  // -------------------------------------------------------------------

  /**
   * Displays the status of the Mongo database and the Solr indexes.
   */
  protected void displayStatus() throws IndexException {
    // Make sure the Solr indexes are up-to-date
    indexManager.commitAll();

    System.out.println();
    System.out.println(storageManager.getStatus());
    System.out.println(indexManager.getStatus());
  }

  protected <T extends Entity> EntityRef newEntityRef(Class<T> type, T entity) {
    String itype = typeRegistry.getINameForType(type);
    String xtype = typeRegistry.getXNameForType(type);
    return new EntityRef(itype, xtype, entity.getId(), entity.getDisplayName());
  }

  protected <T extends Entity> EntityRef newEntityRef(Class<T> type, String id) {
    String itype = typeRegistry.getINameForType(type);
    String xtype = typeRegistry.getXNameForType(type);
    return new EntityRef(itype, xtype, id, null);
  }

}
