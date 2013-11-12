package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.ToolBase;

public abstract class DefaultImporter extends ToolBase {

  /** File with {@code RelationType} definitions; must be present on classpath. */
  private static final String RELATION_TYPE_DEFS = "relationtype-defs.txt";

  protected final TypeRegistry typeRegistry;
  protected final StorageManager storageManager;
  protected final IndexManager indexManager;

  private String prevMessage;
  private int errors;

  public DefaultImporter(TypeRegistry registry, StorageManager storageManager, RelationManager relationManager, IndexManager indexManager) {
    this.typeRegistry = registry;
    this.storageManager = storageManager;
    this.indexManager = indexManager;
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

  /**
   * Removes the non persisted entity's of {@code type} and it's relations from the storage and the index.
   * Use with project specific entities. If you use generic entities all (including the entities of other projects) non persisted entities will be removed.
   */
  protected void removeNonPersistentEnties(Class<? extends DomainEntity> type, StorageManager storageManager, IndexManager indexManager) throws IOException, IndexException {
    List<String> ids = storageManager.getAllIdsWithoutPIDOfType(type);
    storageManager.removeNonPersistent(type, ids);
    indexManager.deleteEntities(type, ids);
    //Remove relations
    List<String> relationIds = storageManager.getRelationIds(ids);
    storageManager.removeNonPersistent(Relation.class, relationIds);
    indexManager.deleteEntities(Relation.class, ids);
  }

}
