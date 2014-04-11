package nl.knaw.huygens.timbuctoo.validation;

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

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

/**
 * Checks if the relation conforms to the type.
 */
public class RelationTypeConformationValidator implements Validator<Relation> {

  private final StorageManager storage;

  public RelationTypeConformationValidator(StorageManager storage) {
    this.storage = storage;
  }

  @Override
  public void validate(Relation entity) throws ValidationException {
    String relationTypeId = entity.getTypeId();

    RelationType relationType = storage.getRelationTypeById(relationTypeId);
    if (relationType == null) {
      throw new ValidationException("RelationType with id " + relationTypeId + " does not exist");
    }

    if (!entity.conformsToRelationType(relationType)) {
      throw new ValidationException("Relation is not conform the RelationType with id " + relationTypeId);
    }
  }

}
