package nl.knaw.huygens.timbuctoo.tools.importer;

/*
 * #%L
 * Timbuctoo tools
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
 */
public class RelationDTO {

  /** The relation type name. */
  private String typeName;
  /** The internal name of the source entity. */
  private String sourceType;
  /** The field name of the source entity to use as key;
   * if {@code null} the id supplied by the importer is used. */
  private String sourceKey;
  /** The value of the source entity for the specified key. */
  private String sourceValue;
  /** The internal name of the target entity. */
  private String targetType;
  /** The field name of the target entity to use as key;
   * if {@code null} the id supplied by the importer is used. */
  private String targetKey;
  /** The value of the target entity for the specified key. */
  private String targetValue;

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getSourceKey() {
    return sourceKey;
  }

  public void setSourceKey(String sourceKey) {
    this.sourceKey = sourceKey;
  }

  public String getSourceValue() {
    return sourceValue;
  }

  public void setSourceValue(String sourceValue) {
    this.sourceValue = sourceValue;
  }

  public String getTargetType() {
    return targetType;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  public String getTargetKey() {
    return targetKey;
  }

  public void setTargetKey(String targetKey) {
    this.targetKey = targetKey;
  }

  public String getTargetValue() {
    return targetValue;
  }

  public void setTargetValue(String targetValue) {
    this.targetValue = targetValue;
  }

}
