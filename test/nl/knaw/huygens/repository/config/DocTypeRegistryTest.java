package nl.knaw.huygens.repository.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

  @Test
  public void testConstructModelToRegister() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromWebServiceTypeString("testextrabasedocs"));
  }

  @Test
  public void testGetClassFromWebServiceTypeStringeAllLowerCase() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromWebServiceTypeString("testextrabasedocs"));
  }

  @Test
  public void testGetClassFromWebServiceTypeStringWithCapitals() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getClassFromWebServiceTypeString("TestExtraBaseDocs"));
  }

  @Test
  public void testGetClassFromWebServiceTypeStringAllUppercase() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getClassFromWebServiceTypeString("TESTEXTRABASEDOCs"));
  }

  @Test
  public void testGetCollectionIdFromCollectionBaseClass() {
    DocTypeRegistry registry = new DocTypeRegistry(null);
    assertEquals("testbasedoc", registry.getCollectionId(TestBaseDoc.class));
  }

  @Test
  public void testGetCollectionIdFromCollectionClass() {
    DocTypeRegistry registry = new DocTypeRegistry(null);
    assertEquals("testconcretedoc", registry.getCollectionId(TestConcreteDoc.class));
  }

  @Test
  public void testGetCollectionIdFromNonDirectDescendantOfDocument() {
    DocTypeRegistry registry = new DocTypeRegistry(null);
    assertEquals("testinheritsfromtestbasedoc", registry.getCollectionId(TestInheritsFromTestBaseDoc.class));
  }

  @Test
  public void testGetCollectionIdForARegisteredClass() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertEquals("testconcretedoc", registry.getCollectionId(TestConcreteDoc.class));
  }

  @Test
  public void testRegisterPackageReadablePackage() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromWebServiceTypeString("testextrabasedocs"));
  }

  @Test
  public void testRegisterPackageDontRegisterClass() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getClassFromWebServiceTypeString("donotregistertests"));
  }

  @Test
  public void testRegisterPackageNonDocument() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getClassFromWebServiceTypeString("nonDoc"));
  }

  @Test
  public void testRegisterPackageSubPackage() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getClassFromWebServiceTypeString("testdoc"));
  }

  @Test
  public void testRegisterPackageAbstractClass() {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGE);
    assertNull(registry.getClassFromWebServiceTypeString("testbasedoc"));
  }

}
