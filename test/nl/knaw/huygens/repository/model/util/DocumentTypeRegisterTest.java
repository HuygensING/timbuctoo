package nl.knaw.huygens.repository.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.Search;
import nl.knaw.huygens.repository.model.Sitemap;
import nl.knaw.huygens.repository.model.User;
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

  private DocumentTypeRegister registry;

  @Before
  public void setUp() throws Exception {
    registry = new DocumentTypeRegister();
  }

  @After
  public void tearDown() {
    registry = null;
  }

  @Test
  public void testGetClassFromTypeStringAllLowerCase() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test
  public void testGetClassFromTypeStringWithCapitals() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("TestExtraBaseDoc");

    assertNull(testExtraBaseDocClass);
  }

  @Test
  public void testGetClassFromTypeStringAllUppercase() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("TESTEXTRABASEDOC");

    assertNull(testExtraBaseDocClass);
  }

  @Test
  public void testGetClassFromTypeStringWithPackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("model-testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test
  public void testGetCollectionIdFromCollectionBaseClass() {
    String actual = registry.getCollectionId(TestBaseDoc.class);
    String expected = "testbasedoc";

    assertEquals(expected, actual);
  }

  @Test
  public void testGetCollectionIdFromCollectionClass() {
    String actual = registry.getCollectionId(TestDoc.class);
    String expected = "testbasedoc";

    assertEquals(expected, actual);
  }

  @Test
  public void testConstructor() {
    // By default all the concrete classes of the package of Document are
    // loaded.
    Class<?> personClass = registry.getClassFromTypeString("person");
    Class<?> searchClass = registry.getClassFromTypeString("search");
    Class<?> sitemapClass = registry.getClassFromTypeString("sitemap");
    Class<?> userClass = registry.getClassFromTypeString("user");

    Class<?> notInDocumentPackage = registry.getClassFromTypeString("notindocumentpackage");

    assertEquals(Person.class, personClass);
    assertEquals(Search.class, searchClass);
    assertEquals(Sitemap.class, sitemapClass);
    assertEquals(User.class, userClass);

    assertNull(notInDocumentPackage);

  }

  @Test
  public void testRegisterPackageFromClassWithDocumentSubClass() {
    registry.registerPackageFromClass(TestBaseDoc.class);

    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterPackageFromClassWithNull() {
    registry.registerPackageFromClass(null);
  }

  @Test
  public void testRegisterPackageReadablePackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");

    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");

    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
  }

  @Test
  public void testRegisterPackageDoesNotRemoveDefaultPackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> userClass = registry.getClassFromTypeString("user");
    assertEquals(User.class, userClass);
  }

  @Test
  public void testRegisterPackageNonDocument() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");

    Class<?> testNonDoc = registry.getClassFromTypeString("nonDoc");
    assertNull(testNonDoc);
  }

  @Test
  public void testRegisterPackageSubPackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");
    Class<?> testDocClass = registry.getClassFromTypeString("testdoc");

    assertNull(testDocClass);
  }

  @Test
  public void testRegisterPackageAbstractClass() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");

    Class<?> testBaseDocClass = registry.getClassFromTypeString("testbasedoc");

    assertNull(testBaseDocClass);
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterPackageNullPackage() {
    registry.registerPackage(null);
  }

  @Test
  public void testRegisterPackageUnReadablePackage() {
    fail("Yet to be implemented.");
  }

}
