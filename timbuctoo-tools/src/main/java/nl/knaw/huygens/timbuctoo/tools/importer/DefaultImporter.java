package nl.knaw.huygens.timbuctoo.tools.importer;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
   * Deletes the non persisted entity's of {@code type} and it's relations from the storage and the index.
   * Use with project specific entities. If you use generic entities all (including the entities of other projects) non persisted entities will be removed.
   */
  protected void removeNonPersistentEntities(Class<? extends DomainEntity> type) throws IOException, IndexException {
    List<String> ids = storageManager.getAllIdsWithoutPIDOfType(type);

    Class<? extends DomainEntity> baseType = TypeRegistry.toDomainEntity(typeRegistry.getBaseClass(type));

    storageManager.deleteNonPersistent(type, ids);
    indexManager.deleteEntities(baseType, ids);
    // Remove relations
    List<String> relationIds = storageManager.getRelationIds(ids);
    storageManager.deleteNonPersistent(Relation.class, relationIds);
    indexManager.deleteEntities(Relation.class, ids);
  }

}
