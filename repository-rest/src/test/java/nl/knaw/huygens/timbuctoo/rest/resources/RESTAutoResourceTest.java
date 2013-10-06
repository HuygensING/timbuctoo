package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
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

import nl.knaw.huygens.timbuctoo.rest.providers.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.rest.providers.model.TestConcreteDoc;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class RESTAutoResourceTest extends WebServiceTestSetup {

  private static final String USER_ROLE = "USER";

  @Test
  public void testGetDocExisting() {
    String id = "TEST000000000001";
    TestConcreteDoc expectedDoc = new TestConcreteDoc(id);
    when(getStorageManager().getEntity(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    TestConcreteDoc actualDoc = autoResource().path("testconcretedocs").path(id).get(TestConcreteDoc.class);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocNonExistingInstance() {
    String id = "TST0000000001";
    when(getStorageManager().getEntity(TestConcreteDoc.class, id)).thenReturn(null);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocNonExistingClass() {
    ClientResponse response = autoResource().path("unknown").path("TST0000000001").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetAllDocs() {
    List<TestConcreteDoc> expectedList = Lists.newArrayList();
    expectedList.add(new TestConcreteDoc("TST0000000001"));
    expectedList.add(new TestConcreteDoc("TST0000000002"));
    expectedList.add(new TestConcreteDoc("TST0000000003"));
    when(getStorageManager().getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = autoResource().path("testconcretedocs").get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllDocsNonFound() {
    List<TestConcreteDoc> expectedList = Lists.newArrayList();
    when(getStorageManager().getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = autoResource().path("testconcretedocs").get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocExistingDocument() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "TEST000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
  }

  @Test()
  @SuppressWarnings("unchecked")
  public void testPutDocInvalidDocument() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "TEST000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

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

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocNonExistingDocument() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "NULL000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws IOException {
        // only if the document version does not exist an IOException is thrown.
        throw new IOException();
      }
    }).when(getStorageManager()).modifyEntity(any(Class.class), any(TestConcreteDoc.class));

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testPutDocNonExistingType() {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "TEST000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);

    ClientResponse response = autoResource().path("unknown").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testPutDocWrongType() {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));
    String id = "TEST000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);

    ClientResponse response = autoResource().path("otherdocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @Test
  public void testPutOnSuperClass() {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "TEST000000000001";
    GeneralTestDoc doc = new GeneralTestDoc(id);

    ClientResponse response = autoResource().path("otherdocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @Test
  public void testPutOnCollection() {
    String id = "TEST000000000001";
    GeneralTestDoc doc = new GeneralTestDoc(id);

    ClientResponse response = autoResource().path("otherdocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPost() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertNotNull(response.getHeaders().getFirst("Location"));
  }

  @Test
  public void testPostNonExistingCollection() {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    ClientResponse response = autoResource().path("unknown").path("all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostOnSuperType() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    GeneralTestDoc doc = new GeneralTestDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostWrongType() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(null);

    ClientResponse response = autoResource().path("otherdocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  @Test
  public void testPostSpecificDocument() {
    String id = "TEST000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    doc.name = "test";

    ClientResponse response = autoResource().path("otherdocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  @Test
  public void testDelete() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "TEST000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    when(getStorageManager().getEntity(TestConcreteDoc.class, id)).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteDocumentDoesNotExist() {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "TEST000000000001";
    when(getStorageManager().getEntity(TestConcreteDoc.class, id)).thenReturn(null);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteTypeDoesNotExist() {
    setUpUserRoles(USER_ID, null);

    String id = "TEST000000000001";
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));
    when(getStorageManager().getEntity(TestConcreteDoc.class, id)).thenReturn(null);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteCollection() {
    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  // Security tests

  @Test
  public void testGetDocNotLoggedIn() {
    String id = "TEST000000000001";
    TestConcreteDoc expectedDoc = new TestConcreteDoc(id);
    when(getStorageManager().getEntity(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocEmptyAuthorizationKey() {
    String id = "TEST000000000001";
    TestConcreteDoc expectedDoc = new TestConcreteDoc(id);
    when(getStorageManager().getEntity(TestConcreteDoc.class, id)).thenReturn(expectedDoc);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocUserNotInRole() throws IOException {
    setUpUserRoles(USER_ID, null);

    String id = "TEST000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocUserNotLoggedIn() throws IOException {
    String id = "TEST000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    setUserUnauthorized();

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostUserNotInRole() throws IOException {
    setUpUserRoles(USER_ID, null);

    TestConcreteDoc inputDoc = new TestConcreteDoc();
    inputDoc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(inputDoc);

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, inputDoc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostUserNotLoggedIn() throws IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    setUserUnauthorized();

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteNotLoggedIn() {
    String id = "TEST000000000001";

    setUserUnauthorized();

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotInRole() {
    setUpUserRoles(USER_ID, null);

    String id = "TEST000000000001";

    ClientResponse response = autoResource().path("testconcretedocs").path(id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  // Variation tests

  @Test
  public void testGetDocOfVariation() {
    String id = "TEST000000000001";
    TestConcreteDoc expectedDoc = new TestConcreteDoc(id);
    String variation = "projecta";
    when(getStorageManager().getCompleteVariation(TestConcreteDoc.class, id, variation)).thenReturn(expectedDoc);

    TestConcreteDoc actualDoc = autoResource().path("testconcretedocs").path(id).path(variation).header("Authorization", "bearer 12333322abef").get(TestConcreteDoc.class);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocOfVariationDocDoesNotExist() {
    String id = "TEST000000000002";
    String variation = "projecta";
    when(getStorageManager().getCompleteVariation(TestConcreteDoc.class, id, variation)).thenReturn(null);

    ClientResponse response = autoResource().path("testconcretedocs").path(id).path(variation).header("Authorization", "bearer 12333322abef").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

}
