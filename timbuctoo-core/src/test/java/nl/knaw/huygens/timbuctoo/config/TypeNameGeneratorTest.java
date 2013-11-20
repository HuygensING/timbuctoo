package nl.knaw.huygens.timbuctoo.config;

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.VTestSystemEntity;

import org.junit.Test;

public class TypeNameGeneratorTest {

  @Test
  public void testGetInternalName() {
    assertEquals("generaltestdoc", TypeNameGenerator.getInternalName(GeneralTestDoc.class));
  }

  @Test
  public void testGetExternalName() {
    assertEquals("generaltestdocs", TypeNameGenerator.getExternalName(GeneralTestDoc.class));
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
