package nl.knaw.huygens.timbuctoo.config;

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;
import nl.knaw.huygens.timbuctoo.variation.model.VTestSystemEntity;

import org.junit.Test;

public class TypeNameGeneratorTest {

  @Test
  public void testGetInternalName() {
    assertEquals("testconcretedoc", TypeNameGenerator.getInternalName(TestConcreteDoc.class));
  }

  @Test
  public void testGetExternalName() {
    assertEquals("testconcretedocs", TypeNameGenerator.getExternalName(TestConcreteDoc.class));
  }

  @Test
  public void testGetInternalNameForAnnotation() {
    assertEquals("vtestsystementity", TypeNameGenerator.getInternalName(VTestSystemEntity.class));
  }

  @Test
  public void testGetExternalNameForAnnotation() {
    assertEquals("mysystementity", TypeNameGenerator.getExternalName(VTestSystemEntity.class));
  }

}
