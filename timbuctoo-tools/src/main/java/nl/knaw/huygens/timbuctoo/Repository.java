package nl.knaw.huygens.timbuctoo;

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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Starting point for modeling the repository as such.
 */
public class Repository {

  private static final Logger LOG = LoggerFactory.getLogger(Repository.class);

  private final TypeRegistry typeRegistry;
  private final StorageManager storageManager;
  private final IndexManager indexManager;

  @Inject
  public Repository(TypeRegistry typeRegistry, StorageManager storageManager, IndexManager indexManager) {
    this.typeRegistry = typeRegistry;
    this.storageManager = storageManager;
    this.indexManager = indexManager;
  }

  public TypeRegistry getTypeRegistry() {
    return typeRegistry;
  }

  public StorageManager getStorageManager() {
    return storageManager;
  }

  public IndexManager getIndexManager() {
    return indexManager;
  }

  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException, ValidationException {
    return storageManager.addDomainEntity(type, entity, change);
  }

  public void close() {
    storageManager.close();
    try {
      indexManager.close();
    } catch (IndexException e) {
      LOG.error("Error while closing index: {}", e.getMessage());
    }
  }

}
