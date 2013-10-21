package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.rest.providers.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.rest.providers.model.TestConcreteDoc;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.verification.VerificationMode;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class DomainEntityResourceTest extends WebServiceTestSetup {

  private static final String DEFAULT_ID = "TEST000000000001";
  private static final String USER_ROLE = "USER";

  @Test
  public void testGetDocExisting() {
    TestConcreteDoc expectedDoc = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(expectedDoc);

    TestConcreteDoc actualDoc = autoResource().path("testconcretedocs").path(DEFAULT_ID).get(TestConcreteDoc.class);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocNonExistingInstance() {
    when(getStorageManager().getEntity(TestConcreteDoc.class, "TST0000000001")).thenReturn(null);

    ClientResponse response = autoResource().path("testconcretedocs").path("TST0000000001").get(ClientResponse.class);
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
  public void testPutDocExistingDocument() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    doc.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
    verifyPersistObject(times(1), "testconcretedoc", DEFAULT_ID);
  }

  protected void verifyPersistObject(VerificationMode invocations, String collection, String id) throws PersistenceException {
    PersistenceWrapper perisistenceWrapper = injector.getInstance(PersistenceWrapper.class);
    verify(perisistenceWrapper, invocations).persistObject(collection, id);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocExistingDocumentWithoutPID() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Ignore
  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocInvalidDocument() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
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

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocNonExistingDocument() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "NULL000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    doc.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
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
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testPutDocNonExistingType() throws PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);

    ClientResponse response = autoResource().path("unknown").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testPutDocWrongType() throws PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));
    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);

    ClientResponse response = autoResource().path("otherdocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testPutOnSuperClass() throws PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    GeneralTestDoc doc = new GeneralTestDoc(DEFAULT_ID);

    ClientResponse response = autoResource().path("otherdocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testPutOnCollection() throws PersistenceException {
    GeneralTestDoc doc = new GeneralTestDoc(DEFAULT_ID);

    ClientResponse response = autoResource().path("otherdocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPost() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);
    when(getStorageManager().addEntity(TestConcreteDoc.class, doc)).thenReturn(DEFAULT_ID);

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertNotNull(response.getHeaders().getFirst("Location"));
    verifyPersistObject(times(1), "testconcretedoc", DEFAULT_ID);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostPersistenceException() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);
    when(getStorageManager().addEntity(TestConcreteDoc.class, doc)).thenReturn(DEFAULT_ID);
    doThrow(PersistenceException.class).when(injector.getInstance(PersistenceWrapper.class)).persistObject(anyString(), anyString());

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertNotNull(response.getHeaders().getFirst("Location"));
  }

  @Test
  public void testPostNonExistingCollection() throws PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    ClientResponse response = autoResource().path("unknown").path("all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostOnSuperType() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    GeneralTestDoc doc = new GeneralTestDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostWrongType() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(null);

    ClientResponse response = autoResource().path("otherdocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testPostSpecificDocument() throws PersistenceException {
    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    doc.name = "test";

    ClientResponse response = autoResource().path("otherdocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testDelete() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    doc.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
    verifyPersistObject(times(1), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testDeleteDocumentWithoutPID() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testDeleteDocumentDoesNotExist() throws PersistenceException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(null);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testDeleteTypeDoesNotExist() throws PersistenceException {
    setUpUserRoles(USER_ID, null);

    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(null);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testDeleteCollection() throws PersistenceException {
    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  // Security tests

  @Test
  public void testGetDocNotLoggedIn() {
    TestConcreteDoc expectedDoc = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(expectedDoc);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocEmptyAuthorizationKey() {
    TestConcreteDoc expectedDoc = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(expectedDoc);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocUserNotInRole() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, null);

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());

    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocUserNotLoggedIn() throws IOException, PersistenceException {
    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    setUserUnauthorized();

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());

    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostUserNotInRole() throws IOException, PersistenceException {
    setUpUserRoles(USER_ID, null);

    TestConcreteDoc inputDoc = new TestConcreteDoc();
    inputDoc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(inputDoc);

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, inputDoc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());

    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostUserNotLoggedIn() throws IOException, PersistenceException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    setUserUnauthorized();

    ClientResponse response = autoResource().path("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());

    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testDeleteNotLoggedIn() throws PersistenceException {
    setUserUnauthorized();

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  @Test
  public void testDeleteUserNotInRole() throws PersistenceException {
    setUpUserRoles(USER_ID, null);

    ClientResponse response = autoResource().path("testconcretedocs").path(DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyPersistObject(never(), "testconcretedoc", DEFAULT_ID);
  }

  // Variation tests

  @Test
  public void testGetDocOfVariation() {
    TestConcreteDoc expectedDoc = new TestConcreteDoc(DEFAULT_ID);
    String variation = "projecta";
    when(getStorageManager().getCompleteVariation(TestConcreteDoc.class, DEFAULT_ID, variation)).thenReturn(expectedDoc);

    TestConcreteDoc actualDoc = autoResource().path("testconcretedocs").path(DEFAULT_ID).path(variation).header("Authorization", "bearer 12333322abef").get(TestConcreteDoc.class);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocOfVariationDocDoesNotExist() {
    String variation = "projecta";
    when(getStorageManager().getCompleteVariation(TestConcreteDoc.class, "TEST000000000002", variation)).thenReturn(null);

    ClientResponse response = autoResource().path("testconcretedocs").path("TEST000000000002").path(variation).header("Authorization", "bearer 12333322abef").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

}
