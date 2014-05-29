package nl.knaw.huygens.timbuctoo.validation;

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

import static nl.knaw.huygens.timbuctoo.model.util.RelationBuilder.newInstance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.DuplicateException;
import nl.knaw.huygens.timbuctoo.storage.Repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RelationDuplicationValidatorTest {

  private Repository repository;
  private RelationDuplicationValidator validator;
  private Relation relation;

  @Before
  public void setup() {
    repository = mock(Repository.class);
    validator = new RelationDuplicationValidator(repository);
    relation = newInstance(Relation.class).withSourceId("id1").withTargetId("id2").withRelationTypeId("id3").build();
  }

  @Test
  public void testValidateNewValidItem() throws Exception {
    when(repository.findRelation(Relation.class, relation)).thenReturn(null);

    validator.validate(relation);

    verify(repository).findRelation(Relation.class, relation);
  }

  @Test(expected = DuplicateException.class)
  public void testValidateItemExists() throws Exception {
    Relation stored = newInstance(Relation.class).withId("storedId").build();
    when(repository.findRelation(Relation.class, relation)).thenReturn(stored);

    try {
      validator.validate(relation);
    } catch (DuplicateException e) {
      Assert.assertEquals("storedId", e.getDuplicateId());
      throw e;
    } finally {
      verify(repository).findRelation(Relation.class, relation);
    }
  }

}
