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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.DuplicateException;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.validation.RelationDuplicationValidator;

import org.junit.Before;
import org.junit.Test;

public class RelationDuplicationValidatorTest {

  private StorageManager storage;
  private RelationDuplicationValidator validator;
  private String firstId = "Id00001";
  private String secondId = "Id00002";
  private String typeId = "typeId";

  @Before
  public void setUp() {
    storage = mock(StorageManager.class);
    validator = new RelationDuplicationValidator(storage);
  }

  @Test
  public void testValidateNewValidItem() throws IOException, ValidationException {
    Relation example = createRelation(firstId, secondId, typeId);

    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    // action
    validator.validate(entityToValidate);

    // verify
    verify(storage).findEntity(Relation.class, example);
  }

  @Test(expected = DuplicateException.class)
  public void testValidateItemExists() throws IOException, ValidationException {
    Relation example = createRelation(firstId, secondId, typeId);
    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    Relation itemFound = createRelation(firstId, secondId, typeId);
    itemFound.setSourceType("sourceType");
    itemFound.setTargetType("targetType");
    itemFound.setTypeType("typeType");

    //when
    when(storage.findEntity(Relation.class, example)).thenReturn(itemFound);

    try {
      // action
      validator.validate(itemFound);
    } finally {
      // verify
      verify(storage).findEntity(Relation.class, example);
      verifyNoMoreInteractions(storage);
    }
  }

  private Relation createRelation(String sourceId, String targetId, String typeId) {
    Relation example = new Relation();
    example.setSourceId(sourceId);
    example.setTargetId(targetId);
    example.setTypeId(typeId);
    return example;
  }

}
