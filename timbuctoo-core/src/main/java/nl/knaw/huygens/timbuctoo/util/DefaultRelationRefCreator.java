package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.google.inject.Inject;

public class DefaultRelationRefCreator implements RelationRefCreator {

  private final TypeRegistry typeRegistry;
  private final Storage storage;

  /**
   * @Inject if you subclass this type make sure this annotation is added to the constructor 
   */
  @Inject
  public DefaultRelationRefCreator(TypeRegistry typeRegistry, Storage storage) {
    this.typeRegistry = typeRegistry;
    this.storage = storage;
  }

  @Override
  public final RelationRef createRegular(EntityMapper mapper, Relation relation, RelationType relType) throws StorageException {
    return createRef(mapper, relation, relType, relation.getTargetType(), relation.getTargetId(), relType.getRegularName());
  }

  private RelationRef createRef(EntityMapper mapper, Relation relation, RelationType relType, String refTypeName, String refId, String relationName) throws StorageException {
    Class<? extends DomainEntity> baseRefType = typeRegistry.getDomainEntityType(refTypeName);
    Class<? extends DomainEntity> refType = mapper.map(baseRefType);

    DomainEntity refEntity = storage.getEntityOrDefaultVariation(refType, refId);

    RelationRef ref = createRef(relation, refId, relationName, refType, refEntity);

    return ref;
  }

  protected RelationRef createRef(Relation relation, String refId, String relationName, Class<? extends DomainEntity> refType, DomainEntity refEntity) {
    return new RelationRef(TypeNames.getInternalName(refType), TypeNames.getExternalName(refType), refId, refEntity.getIdentificationName(), relation.getId(), relation.isAccepted(),
        relation.getRev(), relationName);
  }

  @Override
  public final RelationRef createInverse(EntityMapper mapper, Relation relation, RelationType relType) throws StorageException {
    return createRef(mapper, relation, relType, relation.getSourceType(), relation.getSourceId(), relType.getInverseName());
  }
}
