package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.model.Entity;

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

/**
 * Signals failure of an update because an entity changed.
 */
public class UpdateException extends StorageException {

  private static final long serialVersionUID = 1L;

  public UpdateException() {
    super();
  }

  public UpdateException(String message) {
    super(message);
  }

  public UpdateException(Throwable cause) {
    super(cause);
  }

  public UpdateException(String message, Throwable cause) {
    super(message, cause);
  }

  public static <T extends Entity> UpdateException entityNotFound(Class<T> type, T entity) {
    return new UpdateException(String.format("\"%s\" with id \"%s\" cannot be found.", type.getSimpleName(), entity.getId()));
  }

  public static <T extends Entity> UpdateException revisionNotFound(Class<? super T> type, T entity, int actualLatestRev) {
    return new UpdateException(String.format("\"%s\" with id \"%s\" and revision \"%d\" found. Revision \"%d\" wanted.", type.getSimpleName(), entity.getId(), entity.getRev() - 1, actualLatestRev));
  }

  public static <T extends Entity> UpdateException variantAlreadyExists(Class<T> type, String id) {
    return new UpdateException(String.format("Variant \"%s\" cannot be added to entity with id \"%s\" when it already exists.", type, id));
  }
}
