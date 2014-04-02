package nl.knaw.huygens.timbuctoo.validation;

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
  private RelationTypeConformationValidator relationTypeConformationValidator;
  private RelationValidator instance;
  private Relation relation = new Relation();

  @Before
  public void setUp() {
    relationDuplicationValidatorMock = mock(RelationDuplicationValidator.class);
    relationTypeConformationValidator = mock(RelationTypeConformationValidator.class);
    instance = new RelationValidator(relationTypeConformationValidator, relationDuplicationValidatorMock);
  }

  @Test
  public void testValidate() throws ValidationException, IOException {
    // action
    instance.validate(relation);

    // verify
    InOrder inOrder = Mockito.inOrder(relationTypeConformationValidator, relationDuplicationValidatorMock);
    inOrder.verify(relationTypeConformationValidator).validate(relation);
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
      verify(relationTypeConformationValidator).validate(relation);
      verify(relationDuplicationValidatorMock).validate(relation);
    }
  }

  @Test(expected = ValidationException.class)
  public void testValidateRelationFieldValidatorThrowsAnValidationException() throws ValidationException, IOException {
    // when
    doThrow(ValidationException.class).when(relationTypeConformationValidator).validate(relation);

    try {
      // action
      instance.validate(relation);
    } finally {
      //verify
      verify(relationTypeConformationValidator).validate(relation);
      verifyZeroInteractions(relationDuplicationValidatorMock);
    }
  }
}
