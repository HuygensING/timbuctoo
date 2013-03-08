package nl.knaw.huygens.repository.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.Search;
import nl.knaw.huygens.repository.model.Sitemap;
import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.variation.model.TestBaseDoc;
import nl.knaw.huygens.repository.variation.model.TestExtraBaseDoc;
import nl.knaw.huygens.repository.variation.model.TestNonDoc;
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
    Class<?> docClass = registry.getClassFromTypeString("document");

    assertEquals(Document.class, docClass);
  }

  @Test
  public void testGetClassFromTypeStringWithCapitals() {
    Class<?> docClass = registry.getClassFromTypeString("Document");

    assertEquals(null, docClass);
  }

  @Test
  public void testGetClassFromTypeStringAllUppercase() {
    Class<?> docClass = registry.getClassFromTypeString("DOCUMENT");

    assertEquals(null, docClass);
  }

  @Test
  public void testGetClassFromTypeStringWithPackage() {
    Class<?> docClass = registry.getClassFromTypeString("model-document");

    assertEquals(Document.class, docClass);
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
    // By default all the classes of the package of Document are loaded.
    Class<?> docClass = registry.getClassFromTypeString("document");
    Class<?> personClass = registry.getClassFromTypeString("person");
    Class<?> searchClass = registry.getClassFromTypeString("search");
    Class<?> sitemapClass = registry.getClassFromTypeString("sitemap");
    Class<?> userClass = registry.getClassFromTypeString("user");

    Class<?> notInDocumentPackage = registry.getClassFromTypeString("notindocumentpackage");

    assertEquals(Document.class, docClass);
    assertEquals(Person.class, personClass);
    assertEquals(Search.class, searchClass);
    assertEquals(Sitemap.class, sitemapClass);
    assertEquals(User.class, userClass);

    assertNull(notInDocumentPackage);

  }

  @Test
  public void testRegisterPackageFromClassWithDocumentSubClass() {
    registry.registerPackageFromClass(TestBaseDoc.class);

    Class<?> docClass = registry.getClassFromTypeString("document");
    Class<?> testBaseDocClass = registry.getClassFromTypeString("testbasedoc");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");
    Class<?> testNonDoc = registry.getClassFromTypeString("nonDoc");
    Class<?> testDocClass = registry.getClassFromTypeString("testdocclass");

    // Registering a new package does not remove the default one.
    assertEquals(Document.class, docClass);
    assertEquals(TestBaseDoc.class, testBaseDocClass);
    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
    // Only subclasses of document are registered.
    assertNull(testNonDoc);
    // Sub-packages are not registered.
    assertNull(testDocClass);
  }

  @Test
  public void testRegisterPackageFromClassWithNonDocumentSubClass() {
    registry.registerPackageFromClass(TestNonDoc.class);

    Class<?> docClass = registry.getClassFromTypeString("document");
    Class<?> testBaseDocClass = registry.getClassFromTypeString("testbasedoc");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");
    Class<?> testNonDoc = registry.getClassFromTypeString("nonDoc");
    Class<?> testDocClass = registry.getClassFromTypeString("testdocclass");

    // Registering a new package does not remove the default one.
    assertEquals(Document.class, docClass);
    assertEquals(TestBaseDoc.class, testBaseDocClass);
    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
    // Only subclasses of document are registered.
    assertNull(testNonDoc);
    // Sub-packages are not registered.
    assertNull(testDocClass);
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterPackageFromClassWithNull() {
    registry.registerPackageFromClass(null);
  }

  @Test
  public void testRegisterPackageReadablePackage() {
    registry.registerPackage("nl.knaw.huygens.repository.variation.model");

    Class<?> docClass = registry.getClassFromTypeString("document");
    Class<?> testBaseDocClass = registry.getClassFromTypeString("testbasedoc");
    Class<?> testExtraBaseDocClass = registry.getClassFromTypeString("testextrabasedoc");
    Class<?> testNonDoc = registry.getClassFromTypeString("nonDoc");
    Class<?> testDocClass = registry.getClassFromTypeString("testdocclass");

    // Registering a new package does not remove the default one.
    assertEquals(Document.class, docClass);
    assertEquals(TestBaseDoc.class, testBaseDocClass);
    assertEquals(TestExtraBaseDoc.class, testExtraBaseDocClass);
    // Only subclasses of document are registered.
    assertNull(testNonDoc);
    // Sub-packages are not registered.
    assertNull(testDocClass);
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
