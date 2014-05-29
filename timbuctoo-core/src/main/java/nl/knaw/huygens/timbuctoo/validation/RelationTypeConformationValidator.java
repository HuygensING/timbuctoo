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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

/**
 * Checks if the relation conforms to the type.
 */
class RelationTypeConformationValidator implements Validator<Relation> {

  private final Repository repository;

  public RelationTypeConformationValidator(Repository repository) {
    this.repository = repository;
  }

  @Override
  public void validate(Relation entity) throws ValidationException {
    String relationTypeId = entity.getTypeId();

    RelationType relationType = repository.getRelationType(relationTypeId);
    if (relationType == null) {
      throw new ValidationException("No RelationType with id " + relationTypeId);
    }

    if (!relationType.hasSourceTypeName(entity.getSourceType())) {
      throw new ValidationException("SourceType of Relation does not match RelationType with id " + relationTypeId);
    }
    if (!relationType.hasTargetTypeName(entity.getTargetType())) {
      throw new ValidationException("TargetType of Relation does not match RelationType with id " + relationTypeId);
    }
  }

}
