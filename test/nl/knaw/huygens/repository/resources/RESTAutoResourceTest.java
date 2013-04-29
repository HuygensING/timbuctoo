package nl.knaw.huygens.repository.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import net.handle.hdllib.HandleException;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.projecta.OtherDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class RESTAutoResourceTest extends JerseyTest {
  private static Injector injector;
  private SecurityContext securityContext;
  private OAuthAuthorizationServerConnector oAuthAuthorizationServerConnector;

  @BeforeClass
  public static void setUpClass() {
    injector = Guice.createInjector(new RESTAutoResourceTestModule());
  }

  @Before
  public void setUpAuthorizationServerConnectorMock() {
    securityContext = mock(SecurityContext.class);
    oAuthAuthorizationServerConnector = injector.getInstance(OAuthAuthorizationServerConnector.class);

    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws HandleException {
        String authorizationString = (String) invocation.getArguments()[0];

        if (authorizationString == null || authorizationString.trim().isEmpty()) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return securityContext;
      }
    }).when(oAuthAuthorizationServerConnector).authenticate(anyString());
  }

  @After
  public void tearDownAuthorizationServerConnectorMock() {
    securityContext = null;
    oAuthAuthorizationServerConnector = null;
  }

  public RESTAutoResourceTest() {
    super(new GuiceTestContainerFactory(injector));
  }

  @Override
  protected AppDescriptor configure() {
    WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder("nl.knaw.huygens.repository.resources").build();
    webAppDescriptor.getInitParams().put(PackagesResourceConfig.PROPERTY_PACKAGES, "com.fasterxml.jackson.jaxrs.json,nl.knaw.huygens.repository.providers");
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, "nl.knaw.huygens.repository.server.security.SecurityFilter");
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, "com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory");

    return webAppDescriptor;
  }

  private void setUserInRole(boolean userInRole) {
    when(securityContext.isUserInRole(anyString())).thenReturn(userInRole);
  }

  private void setupDocumentTypeRegister(Class<?> type) {
    DocumentTypeRegister documentTypeRegister = injector.getInstance(DocumentTypeRegister.class);
    doReturn(type).when(documentTypeRegister).getClassFromTypeString(anyString());
  }

  @Test
  public void testGetDocExisting() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    setupDocumentTypeRegister(TestConcreteDoc.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.setId(id);

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    WebResource webResource = super.resource();
    TestConcreteDoc actualDoc = webResource.path("/resources/testconcretedoc/" + id).get(TestConcreteDoc.class);

    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocNonExistingInstance() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    setupDocumentTypeRegister(TestConcreteDoc.class);
    String id = "TST0000000001";

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(null);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetDocNonExistingClass() {
    setupDocumentTypeRegister(null);
    String id = "TST0000000001";

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/unknownclass/" + id).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetAllDocs() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    setupDocumentTypeRegister(TestConcreteDoc.class);

    List<TestConcreteDoc> expectedList = Lists.newArrayList();
    TestConcreteDoc doc1 = new TestConcreteDoc();
    doc1.setId("TST0000000001");
    expectedList.add(doc1);
    TestConcreteDoc doc2 = new TestConcreteDoc();
    doc2.setId("TST0000000002");
    expectedList.add(doc2);
    TestConcreteDoc doc3 = new TestConcreteDoc();
    doc3.setId("TST0000000001");
    expectedList.add(doc3);

    when(storageManager.getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    WebResource webResource = super.resource();

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = webResource.path("/resources/testconcretedoc/all").header("Authorization", "bearer 12333322abef").get(genericType);

    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllDocsNonFound() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    setupDocumentTypeRegister(TestConcreteDoc.class);

    List<TestConcreteDoc> expectedList = Lists.newArrayList();

    when(storageManager.getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    WebResource webResource = super.resource();

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = webResource.path("/resources/testconcretedoc/all").get(genericType);

    assertEquals(expectedList.size(), actualList.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocExistingDocument() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);

    this.setUserInRole(true);

    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.NO_CONTENT, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocInvalidDocument() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);

    this.setUserInRole(true);

    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocNonExistingDocument() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

    this.setUserInRole(true);
    String id = "NEI0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws IOException {
        // only if the document version does not exist an IOException is thrown.
        throw new IOException();
      }
    }).when(storageManager).modifyDocument(any(Class.class), any(TestConcreteDoc.class));

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPutDocNonExistingType() {
    setupDocumentTypeRegister(null);
    this.setUserInRole(true);
    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/unknownDoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPutDocWrongType() {
    setupDocumentTypeRegister(OtherDoc.class);
    this.setUserInRole(true);
    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/otherdoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPutOnSuperClass() {
    setupDocumentTypeRegister(OtherDoc.class);
    this.setUserInRole(true);
    String id = "TST0000000001";

    GeneralTestDoc doc = new GeneralTestDoc();
    doc.setId(id);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/otherdoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPost() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    this.setUserInRole(true);

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.CREATED, clientResponse.getClientResponseStatus());

    assertNotNull(clientResponse.getHeaders().getFirst("Location"));
  }

  @Test
  public void testPostNonExistingCollection() {
    setupDocumentTypeRegister(null);
    this.setUserInRole(true);

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostOnSuperType() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    this.setUserInRole(true);

    GeneralTestDoc doc = new GeneralTestDoc();
    doc.name = "test";

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostWrongType() throws IOException {
    setupDocumentTypeRegister(OtherDoc.class);
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    this.setUserInRole(true);

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(null);

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/otherdoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPostSpecificDocument() {
    this.setUserInRole(true);

    String id = "TST000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    doc.setId(id);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/otherdoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, clientResponse.getClientResponseStatus());
  }

  // Security tests

  @Test
  public void testGetDocNotLoggedIn() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    setupDocumentTypeRegister(TestConcreteDoc.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.setId(id);

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.OK, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetDocEmptyAuthorizationKey() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    setupDocumentTypeRegister(TestConcreteDoc.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.setId(id);

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.OK, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPutDocUserNotLoggedIn() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    DocumentTypeRegister documentTypeRegister = injector.getInstance(DocumentTypeRegister.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.setId(id);

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    doReturn(TestConcreteDoc.class).when(documentTypeRegister).getClassFromTypeString(anyString());

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPostUserNotInRole() {
    DocumentTypeRegister documentTypeRegister = injector.getInstance(DocumentTypeRegister.class);
    this.setUserInRole(false);

    doReturn(TestConcreteDoc.class).when(documentTypeRegister).getClassFromTypeString(anyString());

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.FORBIDDEN, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPostUserNotLoggedIn() {
    setupDocumentTypeRegister(TestConcreteDoc.class);

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());
  }

  // Variation tests

  @Test
  public void testGetDocOfVariation() {
    this.setUserInRole(true);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    setupDocumentTypeRegister(TestConcreteDoc.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.setId(id);

    String variation = "projecta";
    when(storageManager.getCompleteVariation(TestConcreteDoc.class, id, variation)).thenReturn(expectedDoc);

    WebResource webResource = super.resource();
    TestConcreteDoc actualDoc = webResource.path("/resources/testconcretedoc/" + id + "/" + variation).header("Authorization", "bearer 12333322abef").get(TestConcreteDoc.class);

    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocOfVariationDocDoesNotExist() {
    this.setUserInRole(true);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    setupDocumentTypeRegister(TestConcreteDoc.class);
    String id = "TST0000000002";

    String variation = "projecta";
    when(storageManager.getCompleteVariation(TestConcreteDoc.class, id, variation)).thenReturn(null);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id + "/" + variation).header("Authorization", "bearer 12333322abef").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

}
