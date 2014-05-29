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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.junit.Test;

public class RelationTest {

  @Test
  public void testNormalizeSymmetricRelation() throws IOException, ValidationException {
    Relation relation1 = getNormalizedRelation("ID00001", "ID00002", true);
    assertEquals("ID00001", relation1.getSourceId());
    assertEquals("ID00002", relation1.getTargetId());

    Relation relation2 = getNormalizedRelation("ID00002", "ID00001", true);
    assertEquals("ID00001", relation2.getSourceId());
    assertEquals("ID00002", relation2.getTargetId());
  }

  @Test
  public void testNormalizeASymmetricRelation() throws IOException, ValidationException {
    Relation relation1 = getNormalizedRelation("ID00001", "ID00002", false);
    assertEquals("ID00001", relation1.getSourceId());
    assertEquals("ID00002", relation1.getTargetId());

    Relation relation2 = getNormalizedRelation("ID00002", "ID00001", false);
    assertEquals("ID00002", relation2.getSourceId());
    assertEquals("ID00001", relation2.getTargetId());
  }

  private Relation getNormalizedRelation(String sourceId, String targetId, boolean symmetric) {
    Repository repository = mock(Repository.class);

    Relation relation = new Relation();
    relation.setTypeId("typeId");
    relation.setSourceId(sourceId);
    relation.setTargetId(targetId);

    RelationType relationType = new RelationType();
    relationType.setSymmetric(symmetric);
    when(repository.getRelationType("typeId")).thenReturn(relationType);

    relation.normalize(repository);
    return relation;
  }

}
