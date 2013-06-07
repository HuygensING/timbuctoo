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

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.projecta.OtherDoc;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class RESTAutoResourceTest extends WebServiceTestSetup {
  private void setupDocumentTypeRegister(Class<?> type) {
    DocTypeRegistry documentTypeRegister = injector.getInstance(DocTypeRegistry.class);
    doReturn(type).when(documentTypeRegister).getClassFromWebServiceTypeString(anyString());
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
    List<TestConcreteDoc> actualList = webResource.path("/resources/testconcretedoc/all").get(genericType);

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
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));

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
  @Test()
  public void testPutDocInvalidDocument() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));

    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    Validator validator = injector.getInstance(Validator.class);
    // Mockito could not mock the ConstraintViolation, it entered an infinit loop.
    ConstraintViolation<TestConcreteDoc> violation = new ConstraintViolation<TestConcreteDoc>() {

      @Override
      public String getMessage() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getMessageTemplate() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public TestConcreteDoc getRootBean() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Class<TestConcreteDoc> getRootBeanClass() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Object getLeafBean() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Path getPropertyPath() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Object getInvalidValue() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ConstraintDescriptor<?> getConstraintDescriptor() {
        // TODO Auto-generated method stub
        return null;
      }
    };

    when(validator.validate(doc)).thenReturn(Sets.<ConstraintViolation<TestConcreteDoc>> newHashSet(violation));

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocNonExistingDocument() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

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
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
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
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
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
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    String id = "TST0000000001";

    GeneralTestDoc doc = new GeneralTestDoc();
    doc.setId(id);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/otherdoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPutOnCollection() {
    setupDocumentTypeRegister(OtherDoc.class);
    String id = "TST0000000001";

    GeneralTestDoc doc = new GeneralTestDoc();
    doc.setId(id);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/otherdoc").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPost() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

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
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));

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
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

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
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(null);

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/otherdoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPostSpecificDocument() {
    String id = "TST000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    doc.setId(id);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/otherdoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDelete() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    when(storageManager.getDocument(TestConcreteDoc.class, id)).thenReturn(doc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.OK, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteDocumentDoesNotExist() {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    String id = "TST0000000001";

    when(storageManager.getDocument(TestConcreteDoc.class, id)).thenReturn(null);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteTypeDoesNotExist() {
    setUpUserRoles(USER_ID, null);
    String id = "TST0000000001";

    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));

    StorageManager storageManager = mock(StorageManager.class);

    when(storageManager.getDocument(TestConcreteDoc.class, id)).thenReturn(null);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteCollection() {
    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);

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

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocUserNotInRole() throws IOException {
    DocTypeRegistry documentTypeRegister = injector.getInstance(DocTypeRegistry.class);
    setUpUserRoles(USER_ID, null);
    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    doReturn(TestConcreteDoc.class).when(documentTypeRegister).getClassFromWebServiceTypeString(anyString());

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.FORBIDDEN, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocUserNotLoggedIn() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostUserNotInRole() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    setUpUserRoles(USER_ID, null);
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

    TestConcreteDoc inputDoc = new TestConcreteDoc();
    inputDoc.name = "test";

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(inputDoc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .post(ClientResponse.class, inputDoc);

    assertEquals(ClientResponse.Status.FORBIDDEN, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostUserNotLoggedIn() throws IOException {
    setupDocumentTypeRegister(TestConcreteDoc.class);

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, doc);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteNotLoggedIn() {
    String id = "TST0000000001";

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotInRole() {
    setupDocumentTypeRegister(TestConcreteDoc.class);
    setUpUserRoles(USER_ID, null);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    User user = mock(User.class);
    when(user.getRoles()).thenReturn(null);
    when(storageManager.searchDocument(User.class, user)).thenReturn(user);

    String id = "TST0000000001";
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId(id);

    WebResource webResource = super.resource();
    ClientResponse clientResponse = webResource.path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.FORBIDDEN, clientResponse.getClientResponseStatus());
  }

  // Variation tests

  @Test
  public void testGetDocOfVariation() {
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
