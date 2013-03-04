package nl.knaw.huygens.repository.model;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Test;

import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

public class DocumentValidationTest {

  @Test
  public void test() {
    Document doc = new TestDoc();
    ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
    Validator validator = vf.getValidator();
    Set<ConstraintViolation<Document>> errors = validator.validate(doc);
    assertEquals(1, errors.size()); // Should complain about missing ID
    
    doc.setId("Test");
    errors = validator.validate(doc);
    assertEquals(1, errors.size()); // Should complain about wrong ID
    
    doc.setId("abc1234567890");
    errors = validator.validate(doc);
    assertEquals(1, errors.size()); // Should complain about wrong ID
    
    doc.setId("ABC1234567890");
    errors = validator.validate(doc);
    assertEquals(0, errors.size()); // Should be cool now
  }

}
