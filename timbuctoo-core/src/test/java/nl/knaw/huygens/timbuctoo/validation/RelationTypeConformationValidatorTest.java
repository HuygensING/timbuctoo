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
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.MockRelationBuilder;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.junit.Before;
import org.junit.Test;

public class RelationTypeConformationValidatorTest {

  private static final String TYPE_ID = "relationTypeId";

  private Relation relationMock;
  private RelationType relationType;
  private Repository repository;
  private RelationTypeConformationValidator validator;

  @Before
  public void setup() {
    relationMock = MockRelationBuilder.createRelation(Relation.class) //
        .withRelationTypeId(TYPE_ID) //
        .build();

    relationType = new RelationType();
    relationType.setRegularName("testRelation");
    relationType.setSourceTypeName("sourceType");
    relationType.setTargetTypeName("targetType");
    repository = mock(Repository.class);
    validator = new RelationTypeConformationValidator(repository);
  }

  @Test
  public void testValidate() throws IOException, ValidationException {
    when(repository.getRelationTypeById(TYPE_ID)).thenReturn(relationType);
    when(relationMock.getSourceType()).thenReturn("sourceType");
    when(relationMock.getTargetType()).thenReturn("targetType");

    validator.validate(relationMock);
  }

  @Test(expected = ValidationException.class)
  public void testValidateRelationTypeDoesNotExist() throws IOException, ValidationException {
    when(repository.getRelationTypeById(TYPE_ID)).thenReturn(null);
    when(relationMock.getSourceType()).thenReturn("targetType");
    when(relationMock.getTargetType()).thenReturn("sourceType");

    validator.validate(relationMock);
  }

  @Test(expected = ValidationException.class)
  public void testValidateDerivedRelationType() throws IOException, ValidationException {
    relationType.setDerived(true);
    when(repository.getRelationTypeById(TYPE_ID)).thenReturn(relationType);
    when(relationMock.getSourceType()).thenReturn("targetType");
    when(relationMock.getTargetType()).thenReturn("sourceType");

    validator.validate(relationMock);
  }

}
