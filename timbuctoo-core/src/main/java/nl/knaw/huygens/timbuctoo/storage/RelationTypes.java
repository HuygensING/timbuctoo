package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
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

import java.util.List;
import java.util.concurrent.ExecutionException;

import nl.knaw.huygens.timbuctoo.model.RelationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

/**
 * Provides for access to stored relation types.
 */
public class RelationTypes {

  private static final Logger LOG = LoggerFactory.getLogger(RelationTypes.class);

  private final Storage storage;

  /** Caches relation types by id. */
  private LoadingCache<String, RelationType> idCache;

  /** Caches relation types by name. */
  private LoadingCache<String, RelationType> nameCache;

  public RelationTypes(Storage storage) {
    this.storage = storage;
    setupIdCache();
    setupNameCache();
  }

  private void setupIdCache() {
    idCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, RelationType>() {
      @Override
      public RelationType load(String id) throws StorageException {
        RelationType type = storage.getItem(RelationType.class, id);
        if (type == null) {
          // Not allowed to return null
          throw new StorageException("item does not exist");
        }
        return type;
      }
    });
  }

  private void setupNameCache() {
    nameCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, RelationType>() {
      @Override
      public RelationType load(String name) throws StorageException {
        RelationType type = storage.findItemByProperty(RelationType.class, "regularName", name);
        if (type == null) {
          type = storage.findItemByProperty(RelationType.class, "inverseName", name);
        }
        if (type == null) {
          // Not allowed to return null
          throw new StorageException("item does not exist");
        }
        return type;
      }
    });
  }

  public void logCacheStats() {
    LOG.info("RelationType id cache {}", idCache.stats());
    LOG.info("RelationType name cache {}", nameCache.stats());
  }

  /**
   * Returns the relation type with the specified id,
   * or {@code null} if no such relation type exists.
   */
  public RelationType getById(String id) {
    try {
      return idCache.get(id);
    } catch (ExecutionException e) {
      LOG.debug("No relation type with id {}", id);
      return null;
    }
  }

  /**
   * Returns the relation type with the specified name,
   * or {@code null} if no such relation type exists.
   */
  public RelationType getByName(String name) {
    try {
      return nameCache.get(name);
    } catch (ExecutionException e) {
      LOG.debug("No relation type with name {}", name);
      return null;
    }
  }

  /**
   * Get the id's of the relations with their name in {@code relationTypeNames}. 
   * @param relationTypeNames the names to get the id's for.
   * @return a list with id's
   */
  public List<String> getRelationTypeIdsByName(List<String> relationTypeNames) {
    List<String> ids = Lists.newArrayList();

    for (String relationTypeName : relationTypeNames) {
      try {
        RelationType relationType = nameCache.get(relationTypeName);
        if (relationType != null) {
          ids.add(relationType.getId());
        }
      } catch (ExecutionException e) {
        LOG.debug("No relation type with name {}", relationTypeName);
      }
    }
    return ids;
  }
}
