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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RelationManager {

  private static final Logger LOG = LoggerFactory.getLogger(RelationManager.class);

  private final StorageManager storageManager;
  private int duplicateRelationCount;

  @Inject
  public RelationManager(StorageManager storageManager) {
    this.storageManager = storageManager;
    duplicateRelationCount = 0;
  }

  public int getDuplicateRelationCount() {
    return duplicateRelationCount;
  }

  /**
   * Stores the specified relation type.
   */
  public void addRelationType(RelationType type) throws IOException, ValidationException {
    storageManager.addSystemEntity(RelationType.class, type);
  }

  private static final String REGULAR_NAME = FieldMapper.propertyName(RelationType.class, "regularName");

  /**
   * Returns the relation type with the specified name,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationTypeByName(String name) {
    return storageManager.findEntity(RelationType.class, REGULAR_NAME, name);
  }

  /**
   * Returns the relation type with the specified id,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationTypeById(String id) {
    return storageManager.getEntity(RelationType.class, id);
  }

  /**
   * Returns a map for retrieving relation types by their regular name.
   */
  public Map<String, RelationType> getRelationTypeMap() {
    Map<String, RelationType> map = Maps.newHashMap();
    StorageIterator<RelationType> iterator = storageManager.getAll(RelationType.class);
    while (iterator.hasNext()) {
      RelationType type = iterator.next();
      map.put(type.getRegularName(), type);
    }
    return map;
  }

  /**
   * Returns the relation type with the specified reference,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationType(Reference reference) {
    checkArgument(reference.refersToType(RelationType.class), "got type %s", reference.getType());
    return getRelationTypeById(reference.getId());
  }

  public <T extends Relation> String storeRelation(Class<T> type, Reference sourceRef, Reference relTypeRef, Reference targetRef, Change change) {
    T relation = null;
    try {
      relation = type.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create instance of " + type);
    }

    relation.setTypeRef(relTypeRef);

    RelationType relationType = getRelationType(relTypeRef);
    if (relationType == null) {
      LOG.error("Unknown relation type {}", relation.getTypeRef().getId());
      return null;
    }

    // If the relationType is symmetric, order the relation on id.
    // This way we can be sure the relation is saved once.
    if (relationType.isSymmetric() && sourceRef.getId().compareTo(targetRef.getId()) > 0) {
      relation.setSourceRef(targetRef);
      relation.setTargetRef(sourceRef);
    } else {
      relation.setSourceRef(sourceRef);
      relation.setTargetRef(targetRef);
    }

    try {
      return storageManager.addDomainEntity(type, relation, change);
    } catch (DuplicateException e) {
      duplicateRelationCount++;
      LOG.debug("Ignored duplicate {}", relation.getDisplayName());
    } catch (ValidationException e) {
      LOG.error("Failed to add {}; {}", relation.getDisplayName(), e.getMessage());
    } catch (IOException e) {
      LOG.error("Failed to add {}; {}", relation.getDisplayName(), e.getMessage());
    }
    return null;
  }

}
