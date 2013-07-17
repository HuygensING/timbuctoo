package nl.knaw.huygens.repository.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.variation.model.TestBaseDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.repository.variation.model.TestInheritsFromTestBaseDoc;

import org.junit.Test;

/**
 * Tests for the DocTypeRegistry. Watch-out the register is highly
 * dependent on the getCollectionName method. When that
 * implementation changes, a lot of tests will fail.
 * 
 * TODO: remove the dependency of the model-package, in these tests.
 * 
 * @author martijnm
 */
public class DocTypeRegistryTest {

  private static final String MODEL_PACKAGE = "nl.knaw.huygens.repository.variation.model";

  @Test(expected = IllegalArgumentException.class)
  public void testPackageNamesMustnotBeNull() {
    new DocTypeRegistry(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testDocumentTypeNamesMustBeDifferent() {
    new DocTypeRegistry(MODEL_PACKAGE + " " + MODEL_PACKAGE);
  }

  @Test
  public void testGetTypeForIName() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertEquals(TestExtraBaseDoc.class, registry.getTypeForIName("testextrabasedoc"));
  }

  @Test
  public void testGetTypeForXName() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertEquals(TestExtraBaseDoc.class, registry.getTypeForXName("testextrabasedocs"));
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
    Class<? extends Document> baseType = registry.getBaseClass(TestConcreteDoc.class);
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
