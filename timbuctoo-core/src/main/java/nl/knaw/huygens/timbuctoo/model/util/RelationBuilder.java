package nl.knaw.huygens.timbuctoo.model.util;

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

import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;

/**
* Helper class for creating {@code Relation} instances.
*/
public class RelationBuilder<T extends Relation> {

  public static <T extends Relation> RelationBuilder<T> newInstance(Class<T> type) {
    return new RelationBuilder<T>(type);
  }

  protected final Class<T> type;

  protected String typeType;
  protected String typeId;
  protected String sourceType;
  protected String sourceId;
  protected String targetType;
  protected String targetId;

  protected RelationBuilder(Class<T> type) {
    this.type = type;
  }

  public RelationBuilder<T> withRelationTypeRef(Reference ref) {
    typeType = ref.getType();
    typeId = ref.getId();
    return this;
  }

  public RelationBuilder<T> withRelationTypeType(String type) {
    typeType = type;
    return this;
  }

  public RelationBuilder<T> withRelationTypeId(String id) {
    typeId = id;
    return this;
  }

  public RelationBuilder<T> withSourceRef(Reference ref) {
    sourceType = ref.getType();
    sourceId = ref.getId();
    return this;
  }

  public RelationBuilder<T> withSourceType(String type) {
    sourceType = type;
    return this;
  }

  public RelationBuilder<T> withSourceId(String id) {
    sourceId = id;
    return this;
  }

  public RelationBuilder<T> withTargetRef(Reference ref) {
    targetType = ref.getType();
    targetId = ref.getId();
    return this;
  }

  public RelationBuilder<T> withTargeType(String type) {
    targetType = type;
    return this;
  }

  public RelationBuilder<T> withTargetId(String id) {
    targetId = id;
    return this;
  }

  public T build() {
    T relation = newInstance();
    relation.setTypeId(typeId);
    relation.setTypeType(typeType);
    relation.setSourceId(sourceId);
    relation.setSourceType(sourceType);
    relation.setTargetId(targetId);
    relation.setTargetType(targetType);
    return relation;
  }

  private T newInstance() {
    try {
      return type.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create instance of " + type);
    }
  }

}

