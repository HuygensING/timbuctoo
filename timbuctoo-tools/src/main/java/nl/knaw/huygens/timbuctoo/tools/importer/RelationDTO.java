package nl.knaw.huygens.timbuctoo.tools.importer;

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
 * A data transfer object for exporting and importing {@code Relation} entities.
 * 
 * The definition of a relation requires a way of specifying the source and the target entity.
 * The regular entity does this in terms of stored entity id's.
 * Upon import this is usually impossible.
 * A simple way of handling this is by providing a set of id's that are consistent in the set
 * of data to be imported.
 * A more complicated  way is to specify a field and a value of the entity; if this value
 * is unique the entity can be found.
 */
public class RelationDTO {

  public String typeName;
  public String sourceType;
  public String sourceKey;
  public String sourceValue;
  public String targetType;
  public String targetKey;
  public String targetValue;

}
