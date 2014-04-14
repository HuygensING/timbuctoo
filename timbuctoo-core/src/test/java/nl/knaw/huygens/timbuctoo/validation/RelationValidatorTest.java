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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class RelationValidatorTest {

  private RelationDuplicationValidator relationDuplicationValidatorMock;
  private RelationTypeConformationValidator relationTypeConformationValidatorMock;
  private RelationReferenceValidator relationReferenceValidatorMock;
  private RelationValidator instance;
  private Relation relation = new Relation();

  @Before
  public void setUp() {
    relationDuplicationValidatorMock = mock(RelationDuplicationValidator.class);
    relationTypeConformationValidatorMock = mock(RelationTypeConformationValidator.class);
    relationReferenceValidatorMock = mock(RelationReferenceValidator.class);
    instance = new RelationValidator(//
        relationTypeConformationValidatorMock, //
        relationReferenceValidatorMock, //
        relationDuplicationValidatorMock);
  }

  @Test
  public void testValidate() throws ValidationException, IOException {
    // action
    instance.validate(relation);

    // verify
    InOrder inOrder = Mockito.inOrder(relationTypeConformationValidatorMock, relationReferenceValidatorMock, relationDuplicationValidatorMock);
    inOrder.verify(relationTypeConformationValidatorMock).validate(relation);
    inOrder.verify(relationReferenceValidatorMock).validate(relation);
    inOrder.verify(relationDuplicationValidatorMock).validate(relation);
  }

  @Test(expected = ValidationException.class)
  public void testValidateRelationDuplicationValidatorThrowsAnValidationException() throws ValidationException, IOException {
    // when
    doThrow(ValidationException.class).when(relationDuplicationValidatorMock).validate(relation);

    try {
      // action
      instance.validate(relation);
    } finally {
      //verify
      verify(relationTypeConformationValidatorMock).validate(relation);
      verify(relationReferenceValidatorMock).validate(relation);
      verify(relationDuplicationValidatorMock).validate(relation);
    }
  }

  @Test(expected = ValidationException.class)
  public void testValidateRelationTypeConformationValidatorThrowsAnValidationException() throws ValidationException, IOException {
    // when
    doThrow(ValidationException.class).when(relationTypeConformationValidatorMock).validate(relation);

    try {
      // action
      instance.validate(relation);
    } finally {
      //verify
      verify(relationTypeConformationValidatorMock).validate(relation);
      verifyZeroInteractions(relationReferenceValidatorMock, relationDuplicationValidatorMock);
    }
  }

  @Test(expected = ValidationException.class)
  public void testValidateRelationFieldValidatorThrowsAnValidationException() throws ValidationException, IOException {
    // when
    doThrow(ValidationException.class).when(relationReferenceValidatorMock).validate(relation);

    try {
      // action
      instance.validate(relation);
    } finally {
      //verify
      verify(relationTypeConformationValidatorMock).validate(relation);
      verify(relationReferenceValidatorMock).validate(relation);
      verifyZeroInteractions(relationDuplicationValidatorMock);
    }
  }

}
