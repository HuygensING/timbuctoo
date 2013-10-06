package nl.knaw.huygens.timbuctoo.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DocumentTest {

  @Test
  public void testTypeName() {
    Entity doc = new ModelWithOverriddenIndexAnnotations();
    assertEquals("ModelWithOverriddenIndexAnnotations", doc.getClass().getSimpleName());
  }

}
