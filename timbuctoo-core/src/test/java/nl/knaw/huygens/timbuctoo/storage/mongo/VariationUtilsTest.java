package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectAGeneralTestDoc;

import org.junit.BeforeClass;
import org.junit.Test;

public class VariationUtilsTest {

  private static TypeRegistry registry;

  @BeforeClass
  public static void setUpRegistry() {
    registry = new TypeRegistry("nl.knaw.huygens.timbuctoo.variation.model");
  }

  @Test
  public void testGetPackageName() {
    assertEquals("model", VariationUtils.getPackageName(TestExtraBaseDoc.class));
    assertEquals("projecta", VariationUtils.getPackageName(ProjectAGeneralTestDoc.class));
  }

  @Test
  public void testVariationNameToTypeAllLowerCase() {
    assertEquals(TestExtraBaseDoc.class, VariationUtils.variationNameToType(registry, "testextrabasedoc"));
  }

  @Test
  public void testVariationNameToTypeWithCapitals() {
    assertNull(VariationUtils.variationNameToType(registry, "TestExtraBaseDocs"));
  }

  @Test
  public void testVariationNameToTypeAllUppercase() {
    assertNull(VariationUtils.variationNameToType(registry, "TESTEXTRABASEDOCs"));
  }

  @Test
  public void testVariationNameToTypeWithPackage() {
    assertEquals(TestExtraBaseDoc.class, VariationUtils.variationNameToType(registry, "model-testextrabasedoc"));
  }

}
