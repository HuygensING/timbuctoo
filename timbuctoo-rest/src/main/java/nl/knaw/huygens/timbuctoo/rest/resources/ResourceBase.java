package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
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

import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;

/**
 * Base class for Timbuctoo resources.
 */
public class ResourceBase {

  /**
   * Checks the specified condition
   * and throws a {@code TimbuctooException} if the condition is {@code false}.
   */
  protected void checkCondition(boolean condition, Status status, String errorMessageTemplate, Object... errorMessageArgs) {
    if (!condition) {
      throw new TimbuctooException(status, errorMessageTemplate, errorMessageArgs);
    }
  }

  /**
   * Checks the specified reference
   * and throws a {@code TimbuctooException} if the reference is {@code null}.
   */
  protected <T> void checkNotNull(T reference, Status status, String errorMessageTemplate, Object... errorMessageArgs) {
    checkCondition(reference != null, status, errorMessageTemplate, errorMessageArgs);
  }

}
