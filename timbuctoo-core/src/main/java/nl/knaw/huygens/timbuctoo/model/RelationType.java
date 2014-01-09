package nl.knaw.huygens.timbuctoo.model;

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

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.config.TypeNames;

/**
 * The type of a relation between domain entities.
 *
 * (This resembles an RDF predicate.)
 */
@IDPrefix(RelationType.ID_PREFIX)
public class RelationType extends SystemEntity {
  public static final String ID_PREFIX = "RELT";

  /** The name of this relation type. */
  private String regularName;
  /** The name of this relation type when source and target are interchanged. */
  private String inverseName;
  /** The internal type name of the 'active' participant of the relation. */
  private String sourceTypeName;
  /** The internal type name of the 'passive' participant of the relation. */
  private String targetTypeName;
  /** If source and target types are the same, is relation(A,A) allowed? */
  private boolean reflexive;
  /** If source and target types are the same, does relation(A,B) imply relation(B,A)? */
  private boolean symmetric;

  // For deserialization...
  public RelationType() {}

  public RelationType(String regularName, String inverseName, Class<? extends DomainEntity> sourceType, Class<? extends DomainEntity> targetType) {
    this.regularName = regularName;
    this.inverseName = inverseName;
    this.sourceTypeName = TypeNames.getInternalName(sourceType);
    this.targetTypeName = TypeNames.getInternalName(targetType);
    this.reflexive = sourceType.equals(targetType);
    this.symmetric = false;
  }

  public RelationType(String name, Class<? extends DomainEntity> type) {
    this(name, name, type, type);
  }

  @Override
  public String getDisplayName() {
    return regularName;
  }

  public String getRegularName() {
    return regularName;
  }

  public void setRegularName(String name) {
    regularName = name;
  }

  public String getInverseName() {
    return inverseName;
  }

  public void setInverseName(String name) {
    inverseName = name;
  }

  public String getSourceTypeName() {
    return sourceTypeName;
  }

  public void setSourceTypeName(String name) {
    sourceTypeName = name;
  }

  public String getTargetTypeName() {
    return targetTypeName;
  }

  public void setTargetDocType(String name) {
    targetTypeName = name;
  }

  public boolean isReflexive() {
    return reflexive;
  }

  public void setReflexive(boolean reflexive) {
    this.reflexive = reflexive;
  }

  public boolean isSymmetric() {
    return symmetric;
  }

  public void setSymmetric(boolean symmetric) {
    this.symmetric = symmetric;
  }

}
