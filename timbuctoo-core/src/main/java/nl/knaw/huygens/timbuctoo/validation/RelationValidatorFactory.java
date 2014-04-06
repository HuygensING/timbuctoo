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
import nl.knaw.huygens.timbuctoo.storage.Storage;

public class RelationValidatorFactory {
  public final Storage storage;
  private final TypeRegistry typeRegistry;

  public RelationValidatorFactory(Storage storage, TypeRegistry typeRegistry) {
    this.storage = storage;
    this.typeRegistry = typeRegistry;
  }

  public RelationValidator createRelationValidator() {
    return new RelationValidator(new RelationTypeConformationValidator(storage), new RelationReferenceValidator(typeRegistry, storage), new RelationDuplicationValidator(storage));
  }

}
