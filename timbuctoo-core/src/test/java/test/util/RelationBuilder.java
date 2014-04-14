package test.util;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class RelationBuilder {
  private String typeType;
  private String typeId;
  private String sourceId;
  private String targetId;
  private String sourceType;
  private String targetType;

  private RelationBuilder() {

  }

  public static RelationBuilder createRelation() {
    return new RelationBuilder();
  }

  public RelationBuilder withRelationTypeType(String type) {
    this.typeType = type;
    return this;
  }

  public RelationBuilder withRelationTypeId(String id) {
    this.typeId = id;
    return this;
  }

  public RelationBuilder withSourceId(String id) {
    this.sourceId = id;
    return this;
  }

  public RelationBuilder withSourceType(String type) {
    this.sourceType = type;
    return this;
  }

  public RelationBuilder withTargetId(String id) {
    this.targetId = id;
    return this;
  }

  public RelationBuilder withTargeType(String type) {
    this.targetType = type;
    return this;
  }

  public Relation build() {
    Relation relation = new Relation();
    relation.setTypeId(typeId);
    relation.setTypeType(typeType);
    relation.setSourceId(sourceId);
    relation.setSourceType(sourceType);
    relation.setTargetId(targetId);
    relation.setTargetType(targetType);

    return relation;
  }

  public Relation buildMock() {
    Relation relation = mock(Relation.class);
    when(relation.getTypeId()).thenReturn(typeId);
    when(relation.getTypeType()).thenReturn(typeType);
    when(relation.getSourceId()).thenReturn(sourceId);
    when(relation.getSourceType()).thenReturn(sourceType);
    when(relation.getTargetId()).thenReturn(targetId);
    when(relation.getTargetType()).thenReturn(targetType);

    return relation;
  }
}
