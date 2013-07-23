package nl.knaw.huygens.repository.model;

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.repository.index.model.ModelWithOverriddenIndexAnnotations;

import org.junit.Test;

public class DocumentTest {

  @Test
  public void testTypeName() {
    Document doc = new ModelWithOverriddenIndexAnnotations();
    assertEquals("ModelWithOverriddenIndexAnnotations", doc.getTypeName());
  }

}
