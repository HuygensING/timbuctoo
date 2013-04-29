package nl.knaw.huygens.repository.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import nl.knaw.huygens.repository.variation.model.TestBaseDoc;
import nl.knaw.huygens.repository.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

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
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromTypeString("testextrabasedoc"));
  }

  @Test
  public void testGetClassFromTypeStringAllLowerCase() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromTypeString("testextrabasedoc"));
  }

  @Test
  public void testGetClassFromTypeStringWithCapitals() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromTypeString("TestExtraBaseDoc"));
  }

  @Test
  public void testGetClassFromTypeStringAllUppercase() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromTypeString("TESTEXTRABASEDOC"));
  }

  @Test
  public void testGetClassFromTypeStringWithPackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromTypeString("model-testextrabasedoc"));
  }

  @Test
  public void testGetCollectionIdFromCollectionBaseClass() {
    assertEquals("testbasedoc", registry.getCollectionId(TestBaseDoc.class));
  }

  @Test
  public void testGetCollectionIdFromCollectionClass() {
    assertEquals("testbasedoc", registry.getCollectionId(TestDoc.class));
  }

  @Test
  public void testRegisterPackageReadablePackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertEquals(TestExtraBaseDoc.class, registry.getClassFromTypeString("testextrabasedoc"));
  }

  @Test
  public void testRegisterPackageNonDocument() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromTypeString("nonDoc"));
  }

  @Test
  public void testRegisterPackageSubPackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromTypeString("testdoc"));
  }

  @Test
  public void testRegisterPackageAbstractClass() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    assertNull(registry.getClassFromTypeString("testbasedoc"));
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
