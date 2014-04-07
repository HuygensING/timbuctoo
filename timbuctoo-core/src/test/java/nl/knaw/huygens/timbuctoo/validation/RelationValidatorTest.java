package nl.knaw.huygens.timbuctoo.validation;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

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
