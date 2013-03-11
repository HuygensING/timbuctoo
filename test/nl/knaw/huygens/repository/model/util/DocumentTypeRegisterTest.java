package nl.knaw.huygens.repository.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import nl.knaw.huygens.repository.variation.model.TestBaseDoc;
import nl.knaw.huygens.repository.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

/**
 * Tests for the DocumentTypeRegister. Watch-out the register is highly
 * dependent on the MongoUtils.getCollectionName-method. When that
 * implementation changes, a lot of tests will fail.
 * 
 * TODO: remove the dependency of the model-package, in these tests.
 * 
 * @author martijnm
 * 
 */
public class DocumentTypeRegisterTest {

  @Test
  public void testConstructModelToRegister() {
    DocumentTypeRegister registry = new DocumentTypeRegister("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test
  public void testGetClassFromTypeStringAllLowerCase() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test
  public void testGetClassFromTypeStringWithCapitals() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("TestExtraBaseDoc");

    assertNull(testExtraBaseDocClass);
  }

  @Test
  public void testGetClassFromTypeStringAllUppercase() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("TESTEXTRABASEDOC");

    assertNull(testExtraBaseDocClass);
  }

  @Test
  public void testGetClassFromTypeStringWithPackage() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("model-testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test
  public void testGetCollectionIdFromCollectionBaseClass() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    String actual = registry.getCollectionId(TestBaseDoc.class);
    String expected = "testbasedoc";

    assertEquals(expected, actual);
  }

  @Test
  public void testGetCollectionIdFromCollectionClass() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    String actual = registry.getCollectionId(TestDoc.class);
    String expected = "testbasedoc";

    assertEquals(expected, actual);
  }

  @Test
  public void testRegisterPackageFromClassWithDocumentSubClass() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackageFromClass(TestBaseDoc.class);

    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterPackageFromClassWithNull() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackageFromClass(null);
  }

  @Test
  public void testRegisterPackageReadablePackage() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");

    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test
  public void testRegisterPackageNonDocument() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");

    Class<?> testNonDoc = registry.getClassFromTypeString("nonDoc");
    assertNull(testNonDoc);
  }

  @Test
  public void testRegisterPackageSubPackage() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testDocClass = registry.getClassFromTypeString("testdoc");

    assertNull(testDocClass);
  }

  @Test
  public void testRegisterPackageAbstractClass() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");

    Class<?> testBaseDocClass = registry.getClassFromTypeString("testbasedoc");

    assertEquals(TestBaseDoc.class, testBaseDocClass);
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterPackageNullPackage() {
    DocumentTypeRegister registry = new DocumentTypeRegister();
    registry.registerPackage(null);
  }

  @Test
  @Ignore
  public void testRegisterPackageUnReadablePackage() {
    fail("Yet to be implemented.");
  }

}
