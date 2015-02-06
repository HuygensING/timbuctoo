package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
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
 * Signals that an entity was not found in the storage.
 */
public class NoSuchEntityException extends StorageException {

  private static final long serialVersionUID = 1L;

  public NoSuchEntityException() {
    super();
  }

  public NoSuchEntityException(String message) {
    super(message);
  }

  public NoSuchEntityException(Class<? extends Entity> type, String id) {
    this("\"%s\" with \"%s\" does not exist.", TypeNames.getExternalName(type), id);
  }

  public NoSuchEntityException(String format, Object... args) {
    super(String.format(format, args));
  }

  public NoSuchEntityException(Throwable cause) {
    super(cause);
  }

  public NoSuchEntityException(String message, Throwable cause) {
    super(message, cause);
  }

}
