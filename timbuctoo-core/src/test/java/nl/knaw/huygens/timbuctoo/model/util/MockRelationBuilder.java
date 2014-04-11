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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Relation;

/**
* Helper class for creating mock {@code Relation} instances.
*/
public class MockRelationBuilder<T extends Relation> extends RelationBuilder<T> {

  public static <T extends Relation> MockRelationBuilder<T> createRelation(Class<T> type) {
    return new MockRelationBuilder<T>(type);
  }

  protected MockRelationBuilder(Class<T> type) {
    super(type);
  }

  @Override
  public T build() {
    T relation = mock(type);
    when(relation.getTypeType()).thenReturn(typeType);
    when(relation.getTypeId()).thenReturn(typeId);
    when(relation.getSourceType()).thenReturn(sourceType);
    when(relation.getSourceId()).thenReturn(sourceId);
    when(relation.getTargetType()).thenReturn(targetType);
    when(relation.getTargetId()).thenReturn(targetId);
    return relation;
  }

}
