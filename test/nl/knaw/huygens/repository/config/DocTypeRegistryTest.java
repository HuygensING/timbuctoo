package nl.knaw.huygens.repository.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import nl.knaw.huygens.repository.variation.model.TestBaseDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.repository.variation.model.TestInheritsFromTestBaseDoc;

import org.junit.Before;
import org.junit.Ignore;
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

  private DocTypeRegistry registry;

  @Before
  public void setup() {
    registry = new DocTypeRegistry(null);
  }

  @Test
  public void testConstructModelToRegister() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromWebServiceTypeString("testextrabasedocs"));
  }

  @Test
  public void testGetClassFromWebServiceTypeStringeAllLowerCase() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromWebServiceTypeString("testextrabasedocs"));
  }

  @Test
  public void testGetClassFromWebServiceTypeStringWithCapitals() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromWebServiceTypeString("TestExtraBaseDocs"));
  }

  @Test
  public void testGetClassFromWebServiceTypeStringAllUppercase() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromWebServiceTypeString("TESTEXTRABASEDOCs"));
  }

  @Test
  public void testGetClassFromWebServiceTypeStringWithPackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromWebServiceTypeString("model-testextrabasedocs"));
  }

  @Test
  public void testGetClassFromMongoTypeStringeAllLowerCase() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromMongoTypeString("testextrabasedoc"));
  }

  @Test
  public void testGetClassFromMongoTypeStringWithCapitals() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromMongoTypeString("TestExtraBaseDocs"));
  }

  @Test
  public void testGetClassFromMongoTypeStringAllUppercase() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromMongoTypeString("TESTEXTRABASEDOCs"));
  }

  @Test
  public void testGetClassFromMongoTypeStringWithPackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromMongoTypeString("model-testextrabasedoc"));
  }

  @Test
  public void testGetCollectionIdFromCollectionBaseClass() {
    assertEquals("testbasedoc", registry.getCollectionId(TestBaseDoc.class));
  }

  @Test
  public void testGetCollectionIdFromCollectionClass() {
    assertEquals("testconcretedoc", registry.getCollectionId(TestConcreteDoc.class));
  }

  @Test
  public void testGetCollectionIdFromNonDirectDescendantOfDocument() {
    assertEquals("testinheritsfromtestbasedoc", registry.getCollectionId(TestInheritsFromTestBaseDoc.class));
  }

  @Test
  public void testGetCollectionIdForARegisteredClass() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals("testconcretedoc", registry.getCollectionId(TestConcreteDoc.class));
  }

  @Test
  public void testRegisterPackageReadablePackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromWebServiceTypeString("testextrabasedocs"));
  }

  @Test
  public void testRegisterPackageNonDocument() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromWebServiceTypeString("nonDoc"));
  }

  @Test
  public void testRegisterPackageSubPackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromWebServiceTypeString("testdoc"));
  }

  @Test
  public void testRegisterPackageAbstractClass() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromWebServiceTypeString("testbasedoc"));
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterPackageNullPackage() {
    registry.registerPackage(null);
  }

  @Test
  @Ignore
  public void testRegisterPackageUnReadablePackage() {
    fail("Yet to be implemented.");
  }

}
