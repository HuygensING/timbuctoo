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
import nl.knaw.huygens.timbuctoo.storage.DuplicateException;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

public class RelationDuplicationValidator implements Validator<Relation> {

  private final StorageManager storage;

  public RelationDuplicationValidator(StorageManager storage) {
    this.storage = storage;
  }

  @Override
  public void validate(Relation entity) throws ValidationException {
    Relation example = new Relation();
    example.setSourceId(entity.getSourceId());
    example.setTargetId(entity.getTargetId());
    example.setTypeId(entity.getTypeId());
    Relation foundExample = storage.findEntity(Relation.class, example);
    if (foundExample != null) {
      throw new DuplicateException(foundExample.getId());
    }

    Relation inverseExample = new Relation();
    inverseExample.setSourceId(entity.getTargetId());
    inverseExample.setTargetId(entity.getSourceId());
    inverseExample.setTypeId(entity.getTypeId());
    Relation foundInverse = storage.findEntity(Relation.class, inverseExample);
    if (foundInverse != null) {
      throw new DuplicateException(foundInverse.getId());
    }
  }

}
