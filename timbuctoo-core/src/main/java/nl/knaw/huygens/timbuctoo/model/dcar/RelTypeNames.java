package nl.knaw.huygens.timbuctoo.model.dcar;

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

/**
 * Defines names of relation types. These names must correspond with
 * the ones in {@code RelationType} entities in the data store.
 */
public enum RelTypeNames {

  IS_CREATOR_OF("is_creator_of", "is_created_by"), //
  HAS_ARCHIVE_KEYWORD("has_archive_keyword", "is_archive_keyword_of"), //
  HAS_ARCHIVER_KEYWORD("has_archiver_keyword", "is_archiver_keyword_of"), //
  HAS_LEGISLATION_KEYWORD("has_legislation_keyword", "is_legislation_keyword_of"), //
  HAS_ARCHIVE_PERSON("has_archive_person", "is_archive_person_of"), //
  HAS_ARCHIVER_PERSON("has_archiver_person", "is_archive_person_of"), //
  HAS_LEGISLATION_PERSON("has_legislation_person", "is_legislation_person_of"), //
  HAS_ARCHIVE_PLACE("has_archive_place", "is_archive_place_of"), //
  HAS_ARCHIVER_PLACE("has_archiver_place", "is_archiver_place_of"), //
  HAS_LEGISLATION_PLACE("has_legislation_place", "is_legislation_place_of"), //
  HAS_PARENT_ARCHIVE("has_parent_archive", "has_child_archive"), //
  HAS_SIBLING_ARCHIVE("has_sibling_archive", "has_sibling_archive"), //
  HAS_SIBLING_ARCHIVER("has_sibling_archiver", "has_sibling_archiver");

  public final String regular;
  public final String inverse;

  private RelTypeNames(String regular, String inverse) {
    this.regular = regular;
    this.inverse = inverse;
  }

}
