package nl.knaw.huygens.repository.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
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

import nl.knaw.huygens.repository.storage.StorageManager;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class RESTAutoResourceTest extends WebServiceTestSetup {

  @Test
  public void testGetDocExisting() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc(id);

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    TestConcreteDoc actualDoc = resource().path("/resources/testconcretedoc/" + id).get(TestConcreteDoc.class);

    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocNonExistingInstance() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    String id = "TST0000000001";

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(null);

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocNonExistingClass() {
    ClientResponse response = resource().path("/resources/unknown/TST0000000001").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetAllDocs() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    List<TestConcreteDoc> expectedList = Lists.newArrayList();
    expectedList.add(new TestConcreteDoc("TST0000000001"));
    expectedList.add(new TestConcreteDoc("TST0000000002"));
    expectedList.add(new TestConcreteDoc("TST0000000003"));
    when(storageManager.getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = resource().path("/resources/testconcretedoc/all").get(genericType);

    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllDocsNonFound() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    List<TestConcreteDoc> expectedList = Lists.newArrayList();

    when(storageManager.getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = resource().path("/resources/testconcretedoc/all").get(genericType);

    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocExistingDocument() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));

    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc(id);

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test()
  public void testPutDocInvalidDocument() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));

    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc(id);

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    Validator validator = injector.getInstance(Validator.class);
    // Mockito could not mock the ConstraintViolation, it entered an infinit loop.
    ConstraintViolation<TestConcreteDoc> violation = new ConstraintViolation<TestConcreteDoc>() {

      @Override
      public String getMessage() {
        return null;
      }

      @Override
      public String getMessageTemplate() {
        return null;
      }

      @Override
      public TestConcreteDoc getRootBean() {
        return null;
      }

      @Override
      public Class<TestConcreteDoc> getRootBeanClass() {
        return null;
      }

      @Override
      public Object getLeafBean() {
        return null;
      }

      @Override
      public Path getPropertyPath() {
        return null;
      }

      @Override
      public Object getInvalidValue() {
        return null;
      }

      @Override
      public ConstraintDescriptor<?> getConstraintDescriptor() {
        return null;
      }
    };

    when(validator.validate(doc)).thenReturn(Sets.<ConstraintViolation<TestConcreteDoc>> newHashSet(violation));

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocNonExistingDocument() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

    String id = "NEI0000000001";

    TestConcreteDoc doc = new TestConcreteDoc(id);

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws IOException {
        // only if the document version does not exist an IOException is thrown.
        throw new IOException();
      }
    }).when(storageManager).modifyDocument(any(Class.class), any(TestConcreteDoc.class));

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testPutDocNonExistingType() {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc(id);

    ClientResponse response = resource().path("/resources/unknownDoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testPutDocWrongType() {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc(id);

    ClientResponse response = resource().path("/resources/otherdoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @Test
  public void testPutOnSuperClass() {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    String id = "TST0000000001";

    GeneralTestDoc doc = new GeneralTestDoc(id);

    ClientResponse response = resource().path("/resources/otherdoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @Test
  public void testPutOnCollection() {
    String id = "TST0000000001";

    GeneralTestDoc doc = new GeneralTestDoc(id);

    ClientResponse response = resource().path("/resources/otherdoc").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPost() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = resource().path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertNotNull(response.getHeaders().getFirst("Location"));
  }

  @Ignore
  @Test
  public void testPostNonExistingCollection() {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    ClientResponse response = resource().path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostOnSuperType() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

    GeneralTestDoc doc = new GeneralTestDoc();
    doc.name = "test";

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = resource().path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostWrongType() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(null);

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    ClientResponse response = resource().path("/resources/otherdoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @Test
  public void testPostSpecificDocument() {
    String id = "TST000000001";

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    doc.setId(id);

    ClientResponse response = resource().path("/resources/otherdoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  @Test
  public void testDelete() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc(id);

    when(storageManager.getDocument(TestConcreteDoc.class, id)).thenReturn(doc);

    ClientResponse response = resource().path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteDocumentDoesNotExist() {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    String id = "TST0000000001";

    when(storageManager.getDocument(TestConcreteDoc.class, id)).thenReturn(null);

    ClientResponse response = resource().path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteTypeDoesNotExist() {
    setUpUserRoles(USER_ID, null);
    String id = "TST0000000001";

    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));

    StorageManager storageManager = mock(StorageManager.class);

    when(storageManager.getDocument(TestConcreteDoc.class, id)).thenReturn(null);

    ClientResponse response = resource().path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteCollection() {
    ClientResponse response = resource().path("/resources/testconcretedoc").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  // Security tests

  @Test
  public void testGetDocNotLoggedIn() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc(id);

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocEmptyAuthorizationKey() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc(id);

    when(storageManager.getCompleteDocument(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocUserNotInRole() throws IOException {
    setUpUserRoles(USER_ID, null);
    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc(id);

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutDocUserNotLoggedIn() throws IOException {
    String id = "TST0000000001";

    TestConcreteDoc doc = new TestConcreteDoc(id);

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostUserNotInRole() throws IOException {
    setUpUserRoles(USER_ID, null);
    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);

    TestConcreteDoc inputDoc = new TestConcreteDoc();
    inputDoc.name = "test";

    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(inputDoc);

    ClientResponse response = resource().path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .post(ClientResponse.class, inputDoc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostUserNotLoggedIn() throws IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    JacksonJsonProvider jsonProvider = injector.getInstance(JacksonJsonProvider.class);
    when(jsonProvider.readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = resource().path("/resources/testconcretedoc/all").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteNotLoggedIn() {
    String id = "TST0000000001";

    ClientResponse response = resource().path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotInRole() {
    setUpUserRoles(USER_ID, null);

    String id = "TST0000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);

    ClientResponse response = resource().path("/resources/testconcretedoc").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  // Variation tests

  @Test
  public void testGetDocOfVariation() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    String id = "TST0000000001";

    TestConcreteDoc expectedDoc = new TestConcreteDoc(id);

    String variation = "projecta";
    when(storageManager.getCompleteVariation(TestConcreteDoc.class, id, variation)).thenReturn(expectedDoc);

    TestConcreteDoc actualDoc = resource().path("/resources/testconcretedoc/" + id + "/" + variation).header("Authorization", "bearer 12333322abef").get(TestConcreteDoc.class);

    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocOfVariationDocDoesNotExist() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    String id = "TST0000000002";

    String variation = "projecta";
    when(storageManager.getCompleteVariation(TestConcreteDoc.class, id, variation)).thenReturn(null);

    ClientResponse response = resource().path("/resources/testconcretedoc/" + id + "/" + variation).header("Authorization", "bearer 12333322abef").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

}
