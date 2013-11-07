package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectAGeneralTestDoc;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class VariationConverterTest {

  private static TypeRegistry registry;
  private static MongoObjectMapper mongoMapper;

  private VariationConverter base;

  @BeforeClass
  public static void setUpRegistry() {
    registry = new TypeRegistry("timbuctoo.variation.model");
    mongoMapper = new MongoObjectMapper(new MongoFieldMapper());
  }

  @Before
  public void setup() {
    base = new VariationConverter(registry, mongoMapper);
  }

  @Test
  public void testGetPackageName() {
    assertEquals("model", base.getPackageName(TestExtraBaseDoc.class));
    assertEquals("projecta", base.getPackageName(ProjectAGeneralTestDoc.class));
  }

  @Test
  public void testVariationNameToTypeAllLowerCase() {
    assertEquals(TestExtraBaseDoc.class, base.variationNameToType("testextrabasedoc"));
  }

  @Test
  public void testVariationNameToTypeWithCapitals() {
    assertNull(base.variationNameToType("TestExtraBaseDocs"));
  }

  @Test
  public void testVariationNameToTypeAllUppercase() {
    assertNull(base.variationNameToType("TESTEXTRABASEDOCs"));
  }

  @Test
  public void testVariationNameToTypeWithPackage() {
    assertEquals(TestExtraBaseDoc.class, base.variationNameToType("model-testextrabasedoc"));
  }

}
