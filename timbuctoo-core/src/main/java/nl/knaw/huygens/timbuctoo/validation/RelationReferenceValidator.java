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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

class RelationReferenceValidator implements Validator<Relation> {

  private final StorageManager storage;

  public RelationReferenceValidator(StorageManager storage) {
    this.storage = storage;
  }

  @Override
  public void validate(Relation entity) throws ValidationException {
    validateEntityExists(entity.getSourceType(), entity.getSourceId());
    validateEntityExists(entity.getTargetType(), entity.getTargetId());
  }

  private void validateEntityExists(String iname, String id) throws ValidationException {
    TypeRegistry registry = storage.getTypeRegistry();
    if (!storage.entityExists(registry.getDomainEntityType(iname), id)) {
      throw new ValidationException("Entity [%s,%s] does not exist", iname, id);
    }
  }

}
