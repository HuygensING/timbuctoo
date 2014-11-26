package nl.knaw.huygens.timbuctoo.util;

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

import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RelationRefCreator {
  private final TypeRegistry registry;
  private final Storage storage;

  @Inject
  public RelationRefCreator(TypeRegistry registry, Storage storage) {
    this.registry = registry;
    this.storage = storage;
  }

  // Relations are defined between primitive domain entities
  // Map to a domain entity in the package from which an entity is requested
  public RelationRef newRelationRef(EntityMapper mapper, Reference reference, String relationId, boolean accepted, int rev, String relationName) throws StorageException {
    String iname = reference.getType();

    Class<? extends DomainEntity> type = registry.getDomainEntityType(iname);
    Class<? extends DomainEntity> mappedType = mapper.map(type);
    String mappedIName = TypeNames.getInternalName(mappedType);
    String xname = registry.getXNameForIName(mappedIName);
    DomainEntity entity = storage.getItem(mappedType, reference.getId());

    return new RelationRef(mappedIName, xname, reference.getId(), entity.getDisplayName(), relationId, accepted, rev, relationName);
  }

  public RelationRef newReadOnlyRelationRef(String type, String xType, String id, String displayName, String relationName) {
    return new RelationRef(type, xType, id, displayName, null, true, 0, relationName);
  }

  public <T extends DomainEntity> void addReleation(T entity, EntityMapper mapper, Relation relation, RelationType relType) throws StorageException {
    String entityId = entity.getId();
    RelationRef ref = null;
    if (relation.hasSourceId(entityId)) {
      ref = newRelationRef(mapper, relation.getTargetRef(), relation.getId(), relation.isAccepted(), relation.getRev(), relType.getRegularName());
    } else if (relation.hasTargetId(entityId)) {
      ref = newRelationRef(mapper, relation.getSourceRef(), relation.getId(), relation.isAccepted(), relation.getRev(), relType.getInverseName());
    }
    entity.addRelation(ref);
  }

}
