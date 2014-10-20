package nl.knaw.huygens.timbuctoo.model;

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
 * Defines a property of an entity that is actually a property of
 * an entity related with that entity. 
 */
public class DerivedProperty {

  private final String propertyName;
  private final String relationName;
  private final String accessor;

  public DerivedProperty(String propertyName, String relationName, String accessor) {
    this.propertyName = propertyName;
    this.relationName = relationName;
    this.accessor = accessor;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getRelationName() {
    return relationName;
  }

  public String getAccessor() {
    return accessor;
  }

}
