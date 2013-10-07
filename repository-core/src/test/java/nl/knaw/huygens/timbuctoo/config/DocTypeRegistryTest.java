package nl.knaw.huygens.timbuctoo.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestBaseDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestInheritsFromTestBaseDoc;

import org.junit.Test;

/**
 * Tests for the DocTypeRegistry. Watch-out the register is highly
 * dependent on the getCollectionName method. When that
 * implementation changes, a lot of tests will fail.
 * 
 * TODO: remove the dependency of the model-package, in these tests.
 */
public class DocTypeRegistryTest {

  private static final String MODEL_PACKAGE = "timbuctoo.variation.model";

  @Test(expected = IllegalArgumentException.class)
  public void testPackageNamesMustnotBeNull() {
    new DocTypeRegistry(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testEntityTypeNamesMustBeDifferent() {
    new DocTypeRegistry(MODEL_PACKAGE + " " + MODEL_PACKAGE);
  }

  @Test
  public void testGetTypeForIName() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertEquals(GeneralTestDoc.class, registry.getTypeForIName("generaltestdoc"));
    assertEquals(TestExtraBaseDoc.class, registry.getTypeForIName("testextrabasedoc"));
  }

  @Test
  public void testGetTypeForXName() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertEquals(GeneralTestDoc.class, registry.getTypeForXName("generaltestdocs"));
    // TestExtraBaseDoc has @EntityTypeName("testextrabasedoc")
    assertEquals(TestExtraBaseDoc.class, registry.getTypeForXName("testextrabasedoc"));
  }

  @Test
  public void testBaseClassFromCollectionBaseClass() {
    DocTypeRegistry registry = new DocTypeRegistry("");
    assertEquals(TestBaseDoc.class, registry.getBaseClass(TestBaseDoc.class));
  }

  @Test
  public void testBaseClassFromCollectionClass() {
    DocTypeRegistry registry = new DocTypeRegistry("");
    assertEquals(TestConcreteDoc.class, registry.getBaseClass(TestConcreteDoc.class));
  }

  @Test
  public void testGetBaseClassFromNonDirectDescendantOfDocument() {
    DocTypeRegistry registry = new DocTypeRegistry("");
    assertEquals(TestInheritsFromTestBaseDoc.class, registry.getBaseClass(TestInheritsFromTestBaseDoc.class));
  }

  @Test
  public void testGetCollectionIdForARegisteredClass() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    Class<? extends Entity> baseType = registry.getBaseClass(TestConcreteDoc.class);
    assertEquals("testconcretedoc", registry.getINameForType(baseType));
  }

  @Test
  public void testRegisterPackageDontRegisterClass() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getTypeForIName("donotregistertests"));
  }

  @Test
  public void testRegisterPackageNonDocument() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getTypeForIName("nonDoc"));
  }

  @Test
  public void testRegisterPackageSubPackage() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getTypeForIName("testdoc"));
  }

  @Test
  public void testRegisterPackageAbstractClass() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getTypeForIName("testbasedoc"));
  }

}
