package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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
 * Definitions of some resource paths.
 */
public class Paths {

  /** Used for system entities. */
  public static final String SYSTEM_PREFIX = "system";
  /** Used for domain entities. */
  public static final String DOMAIN_PREFIX = "domain";
  /** Regex for determining the entity name.*/
  public static final String ENTITY_REGEX = "[a-zA-Z]+";
  /** Regex for determining the id.*/
  public static final String ID_REGEX = "[a-zA-Z]{4}\\d+";
  /** The path of the {@code UserResource} */
  public static final String USER_PATH = "users";

  private Paths() {
    throw new AssertionError("Non-instantiable class");
  }
}
