package nl.knaw.huygens.timbuctoo.validation;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;

public class RelationValidatorTest {

  private RelationDuplicationValidator relationDuplicationValidatorMock;
  private RelationValidator instance;
  private Relation relation = new Relation();

  @Before
  public void setUp() {
    relationDuplicationValidatorMock = mock(RelationDuplicationValidator.class);
    instance = new RelationValidator(relationDuplicationValidatorMock);
  }

  @Test
  public void testValidate() throws ValidationException, IOException {
    // action
    instance.validate(relation);

    // verify
    verify(relationDuplicationValidatorMock).validate(relation);
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
      verify(relationDuplicationValidatorMock).validate(relation);
    }
  }
}
