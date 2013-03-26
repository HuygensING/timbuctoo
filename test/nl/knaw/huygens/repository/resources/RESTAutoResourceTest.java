package nl.knaw.huygens.repository.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class RESTAutoResourceTest extends JerseyTest {
  private static Injector injector;

  @BeforeClass
  public static void setUpClass() {
    injector = Guice.createInjector(new RESTAutoResourceTestModule());
  }

  public RESTAutoResourceTest() {
    super(new GuiceTestContainerFactory(injector));
  }

  @Override
  protected AppDescriptor configure() {
    return new WebAppDescriptor.Builder("nl.knaw.huygens.repository.resources").build();
  }

  @Test
  public void testGetDocExisting() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    DocumentTypeRegister documentTypeRegister = injector.getInstance(DocumentTypeRegister.class);
    String id = "tst0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.setId(id);

    when(storageManager.getCompleteDocument(id, TestConcreteDoc.class)).thenReturn(expectedDoc);

    doReturn(TestConcreteDoc.class).when(documentTypeRegister).getClassFromTypeString(anyString());

    WebResource webResource = super.resource();
    TestConcreteDoc actualDoc = webResource.path("/resources/testconcretedoc/" + id).get(TestConcreteDoc.class);

    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocNonExistingInstance() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    DocumentTypeRegister documentTypeRegister = injector.getInstance(DocumentTypeRegister.class);
    String id = "tst0000000001";

    when(storageManager.getCompleteDocument(id, TestConcreteDoc.class)).thenReturn(null);

    doReturn(TestConcreteDoc.class).when(documentTypeRegister).getClassFromTypeString(anyString());

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetDocNonExistingClass() {
    DocumentTypeRegister documentTypeRegister = injector.getInstance(DocumentTypeRegister.class);
    String id = "tst0000000001";

    doReturn(null).when(documentTypeRegister).getClassFromTypeString(anyString());

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetAllDocs() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    DocumentTypeRegister documentTypeRegister = injector.getInstance(DocumentTypeRegister.class);

    List<TestConcreteDoc> expectedList = Lists.newArrayList();
    TestConcreteDoc doc1 = new TestConcreteDoc();
    doc1.setId("tst0000000001");
    expectedList.add(doc1);
    TestConcreteDoc doc2 = new TestConcreteDoc();
    doc2.setId("tst0000000002");
    expectedList.add(doc2);
    TestConcreteDoc doc3 = new TestConcreteDoc();
    doc3.setId("tst0000000001");
    expectedList.add(doc3);

    when(storageManager.getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    doReturn(TestConcreteDoc.class).when(documentTypeRegister).getClassFromTypeString(anyString());
    WebResource webResource = super.resource();

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = webResource.path("/resources/testconcretedoc/all").get(genericType);

    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllDocsNonFound() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    DocumentTypeRegister documentTypeRegister = injector.getInstance(DocumentTypeRegister.class);

    List<TestConcreteDoc> expectedList = Lists.newArrayList();

    when(storageManager.getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    doReturn(TestConcreteDoc.class).when(documentTypeRegister).getClassFromTypeString(anyString());
    WebResource webResource = super.resource();

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = webResource.path("/resources/testconcretedoc/all").get(genericType);

    assertEquals(expectedList.size(), actualList.size());
  }
}
