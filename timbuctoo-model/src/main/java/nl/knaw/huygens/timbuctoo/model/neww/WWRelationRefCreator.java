package nl.knaw.huygens.timbuctoo.model.neww;

/*
 * #%L
 * Timbuctoo model
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

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.util.DefaultRelationRefCreator;

import com.google.inject.Inject;

public class WWRelationRefCreator extends DefaultRelationRefCreator {

  @Inject
  public WWRelationRefCreator(TypeRegistry typeRegistry, Storage storage) {
    super(typeRegistry, storage);
  }

  @Override
  protected RelationRef createRef(Relation relation, String refId, String relationName, Class<? extends DomainEntity> refType, DomainEntity refEntity) {
    if (relation instanceof WWRelation) {
      return new WWRelationRef(TypeNames.getInternalName(refType), TypeNames.getExternalName(refType), refId, refEntity.getIdentificationName(), relation.getId(), relation.isAccepted(),
          relation.getRev(), relationName, ((WWRelation) relation).getDate());
    }

    return super.createRef(relation, refId, relationName, refType, refEntity);

  }

}
