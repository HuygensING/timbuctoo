package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.variation.model.TestExtraBaseDoc;

import org.junit.BeforeClass;
import org.junit.Test;

public class VariationUtilsTest {

  private static DocTypeRegistry registry;

  @BeforeClass
  public static void setUpRegistry() {
    registry = new DocTypeRegistry("nl.knaw.huygens.timbuctoo.variation.model");
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
