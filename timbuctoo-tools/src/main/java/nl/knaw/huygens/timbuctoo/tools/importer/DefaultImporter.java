package nl.knaw.huygens.timbuctoo.tools.importer;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

/**
 * A class that contains the default functionality needed in each importer.
 * @author martijnm
 */
public abstract class DefaultImporter {

  protected final TypeRegistry typeRegistry;
  protected final StorageManager storageManager;
  protected final IndexManager indexManager;

  public DefaultImporter(TypeRegistry typeRegistry, StorageManager storageManager, IndexManager indexManager) {
    super();
    this.typeRegistry = typeRegistry;
    this.storageManager = storageManager;
    this.indexManager = indexManager;
  }

  /**
   * Removes the non persisted entity's of {@code type} and it's relations from the storage and the index.
   * Use with project specific entities. If you use generic entities all (including the entities of other projects) non persisted entities will be removed.
   */
  protected void removeNonPersistentEntities(Class<? extends DomainEntity> type) throws IOException, IndexException {
    List<String> ids = storageManager.getAllIdsWithoutPIDOfType(type);

    Class<? extends DomainEntity> baseType = TypeRegistry.toDomainEntity(typeRegistry.getBaseClass(type));

    storageManager.removeNonPersistent(type, ids);
    indexManager.deleteEntities(baseType, ids);
    //Remove relations
    List<String> relationIds = storageManager.getRelationIds(ids);
    storageManager.removeNonPersistent(Relation.class, relationIds);
    indexManager.deleteEntities(Relation.class, ids);
  }

}