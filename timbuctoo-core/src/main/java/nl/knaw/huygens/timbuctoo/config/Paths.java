package nl.knaw.huygens.timbuctoo.config;

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
 * Definitions of some resource paths.
 */
public class Paths {

  /** Used for system entities. */
  public static final String SYSTEM_PREFIX = "system";
  /** Used for domain entities. */
  public static final String DOMAIN_PREFIX = "domain";
  /** Name of the search resource */
  public static final String SEARCH_PATH = "search";

  /** Regex for determining the entity name.*/
  public static final String ENTITY_REGEX = "[a-zA-Z]+";
  /** Regex for determining the id.*/
  public static final String ID_REGEX = "[\\w\\-]+";
  public static final String ID_PARAM = "id";
  public static final String ID_PATH = "/{id: " + ID_REGEX + "}";
  public static final String INDEX_REQUEST_ID_VALUE_PATH = "/{id: " + ID_REGEX + "}";
  public static final String PID_PATH = "/pid";
  public static final String UPDATE_PID_PATH = "/updatepid";
  /** The path of the {@code UserResource} */
  public static final String USER_PATH = "users";
  /** The path for api version 1 */
  public static final String V1_PATH = "v1/";
  public static final String V2_PATH = "v2/";
  public static final String V2_1_PATH = "v2.1/";
  /**
   * When the version 1 path is optional. Contains a slash. 
   * Regex is only allowed for variables, so we assign it apiVersion.
   */
  public static final String VERSION_PARAM = "apiVersion";
  public static final String V1_PATH_OPTIONAL = "{ " + VERSION_PARAM + ": (v1/)? }";
  public static final String V1_TO_V2_PATH = "{ " + VERSION_PARAM + ": (v[1-2]/) }";
  public static final String V1_TO_V2_1_PATH = "{ " + VERSION_PARAM + ": (v2.1/|v[1-2]/) }";
  public static final String V2_OR_V2_1_PATH = "{ " + VERSION_PARAM + ": (v2.1/|v2/) }";
  public static final String VERSION_PATH_OPTIONAL = "{" + VERSION_PARAM + ": (v2.1/|v[0-2]/)? }";

  public static final String ENTITY_PARAM = "entityName";
  public static final String ENTITY_PATH = "{" + ENTITY_PARAM + ": " + ENTITY_REGEX + "}";
  public static final String KEYWORD_PATH = "{" + ENTITY_PARAM + ": [a-z]*keywords}";
  public static final String AUTOCOMPLETE_PATH = "autocomplete";
  public static final String ADMIN_PATH = "admin";
  public static final String INDEX_REQUEST_PATH = "indexrequests";
  public static final String RELATION_PARAM = "relationType";
  public static final String RELATION_SEARCH_PREFIX = "{" + RELATION_PARAM + ": [a-z]*relations }";


  private Paths() {
    throw new AssertionError("Non-instantiable class");
  }
}
