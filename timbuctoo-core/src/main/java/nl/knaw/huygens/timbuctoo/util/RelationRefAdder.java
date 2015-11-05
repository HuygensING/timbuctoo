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
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.google.inject.Singleton;

@Singleton
public class RelationRefAdder {
  private RelationRefCreator relationRefCreator;

  public RelationRefAdder(RelationRefCreator relationRefCreator) {
    this.relationRefCreator = relationRefCreator;
  }

  public <T extends DomainEntity> void addRelation(T entityToAddTo, EntityMapper mapper, Relation relation, RelationType relType) throws StorageException {
    String entityId = entityToAddTo.getId();

    RelationRef ref = null;

    if (relation.hasSourceId(entityId)) {
      ref = relationRefCreator.createRegular(mapper, relation, relType);
    } else {
      ref = relationRefCreator.createInverse(mapper, relation, relType);
    }

    entityToAddTo.addRelation(ref);
  }

  RelationRefCreator getRelationRefCreator() {
    return this.relationRefCreator;
  }
}
