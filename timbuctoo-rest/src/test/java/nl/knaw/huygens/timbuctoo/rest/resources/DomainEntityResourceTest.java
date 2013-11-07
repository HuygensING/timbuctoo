package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

import javax.jms.JMSException;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.rest.providers.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.rest.providers.model.TestConcreteDoc;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class DomainEntityResourceTest extends WebServiceTestSetup {

  private static final String PERSISTENCE_PRODUCER = "persistenceProducer";
  private static final String INDEX_PRODUCER = "indexProducer";
  private static final Class<? extends Entity> DEFAULT_TYPE = TestConcreteDoc.class;
  // private static final String DEFAULT_PID = "c14a5e7d-4728-4f52-af98-480ff7fef08e";
  private static final String DEFAULT_ID = "TEST000000000001";
  private static final String USER_ROLE = "USER";

  @Before
  public void setUpBroker() throws JMSException {
    Broker broker = injector.getInstance(Broker.class);
    when(broker.getProducer(DomainEntityResource.INDEX_MSG_PRODUCER, Broker.INDEX_QUEUE)).thenReturn(getProducer(INDEX_PRODUCER));
    when(broker.getProducer(DomainEntityResource.PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE)).thenReturn(getProducer(PERSISTENCE_PRODUCER));
  }

  private Producer getProducer(String name) {
    return injector.getInstance(Key.get(Producer.class, Names.named(name)));
  }

  @Test
  public void testGetDocExisting() {
    TestConcreteDoc expectedDoc = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntityWithRelations(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(expectedDoc);

    TestConcreteDoc actualDoc = domainResource("testconcretedocs", DEFAULT_ID).get(TestConcreteDoc.class);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocNonExistingInstance() {
    when(getStorageManager().getEntity(TestConcreteDoc.class, "TST0000000001")).thenReturn(null);

    ClientResponse response = domainResource("testconcretedocs", "TST0000000001").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocNonExistingClass() {
    ClientResponse response = domainResource("unknown", "TST0000000001").get(ClientResponse.class);
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
    List<TestConcreteDoc> actualList = domainResource("testconcretedocs").get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllDocsNonFound() {
    List<TestConcreteDoc> expectedList = Lists.newArrayList();
    when(getStorageManager().getAllLimited(TestConcreteDoc.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<TestConcreteDoc>> genericType = new GenericType<List<TestConcreteDoc>>() {};
    List<TestConcreteDoc> actualList = domainResource("testconcretedocs").get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPut() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc entity = new TestConcreteDoc(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(entity);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(entity);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), times(1)).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocExistingDocumentWithoutPID() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc entity = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(entity);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(entity);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocInvalidDocument() throws IOException, PersistenceException, JMSException {
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

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocNonExistingDocument() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    String id = "NULL000000000001";
    TestConcreteDoc doc = new TestConcreteDoc(id);
    doc.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    doThrow(IOException.class).when(getStorageManager()).modifyEntity(Matchers.<Class<TestConcreteDoc>> any(), any(TestConcreteDoc.class));

    ClientResponse response = domainResource("testconcretedocs", id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testPutDocNonExistingType() throws PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);

    ClientResponse response = domainResource("unknown", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testPutDocWrongType() throws PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);

    ClientResponse response = domainResource("otherdocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testPutOnSuperClass() throws PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    GeneralTestDoc doc = new GeneralTestDoc(DEFAULT_ID);

    ClientResponse response = domainResource("otherdocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testPutOnCollection() throws PersistenceException, JMSException {
    GeneralTestDoc doc = new GeneralTestDoc(DEFAULT_ID);

    ClientResponse response = domainResource("otherdocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPost() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);
    when(getStorageManager().addEntity(TestConcreteDoc.class, doc)).thenReturn(DEFAULT_ID);

    ClientResponse response = domainResource("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertNotNull(response.getHeaders().getFirst("Location"));
    verify(getProducer(PERSISTENCE_PRODUCER), times(1)).send(ActionType.ADD, DEFAULT_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.ADD, DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testPostNonExistingCollection() throws PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    ClientResponse response = domainResource("unknown", "all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostOnSuperType() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    GeneralTestDoc doc = new GeneralTestDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = domainResource("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostWrongType() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(null);

    ClientResponse response = domainResource("otherdocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testPostSpecificDocument() throws PersistenceException, JMSException {
    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    doc.name = "test";

    ClientResponse response = domainResource("otherdocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testDelete() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    doc.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(doc);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(ActionType.DEL, DEFAULT_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.DEL, DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testDeleteDocumentWithoutPID() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(doc);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testDeleteDocumentDoesNotExist() throws PersistenceException, JMSException {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(null);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testDeleteTypeDoesNotExist() throws PersistenceException, JMSException {
    setUpUserRoles(USER_ID, null);

    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));
    when(getStorageManager().getEntity(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(null);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testDeleteCollection() throws PersistenceException, JMSException {
    ClientResponse response = domainResource("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  // Security tests

  @Test
  public void testGetDocNotLoggedIn() {
    TestConcreteDoc expectedDoc = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntityWithRelations(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(expectedDoc);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocEmptyAuthorizationKey() {
    TestConcreteDoc expectedDoc = new TestConcreteDoc(DEFAULT_ID);
    when(getStorageManager().getEntityWithRelations(TestConcreteDoc.class, DEFAULT_ID)).thenReturn(expectedDoc);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocUserNotInRole() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, null);

    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutDocUserNotLoggedIn() throws IOException, PersistenceException, JMSException {
    TestConcreteDoc doc = new TestConcreteDoc(DEFAULT_ID);
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    setUserNotLoggedIn();

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostUserNotInRole() throws IOException, PersistenceException, JMSException {
    setUpUserRoles(USER_ID, null);

    TestConcreteDoc inputDoc = new TestConcreteDoc();
    inputDoc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(inputDoc);

    ClientResponse response = domainResource("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").post(ClientResponse.class, inputDoc);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostUserNotLoggedIn() throws IOException, PersistenceException, JMSException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(doc);

    setUserNotLoggedIn();

    ClientResponse response = domainResource("testconcretedocs").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, doc);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testDeleteNotLoggedIn() throws PersistenceException, JMSException {
    setUserNotLoggedIn();

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  @Test
  public void testDeleteUserNotInRole() throws PersistenceException, JMSException {
    setUpUserRoles(USER_ID, null);

    ClientResponse response = domainResource("testconcretedocs", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
    verify(getProducer(INDEX_PRODUCER), never()).send(any(ActionType.class), Matchers.<Class<? extends Entity>> any(), anyString());
  }

  // Variation tests

  @Test
  public void testGetDocOfVariation() {
    TestConcreteDoc expectedDoc = new TestConcreteDoc(DEFAULT_ID);
    String variation = "projecta";
    when(getStorageManager().getCompleteVariation(TestConcreteDoc.class, DEFAULT_ID, variation)).thenReturn(expectedDoc);

    TestConcreteDoc actualDoc = domainResource("testconcretedocs", DEFAULT_ID).path(variation).header("Authorization", "bearer 12333322abef").get(TestConcreteDoc.class);
    assertNotNull(actualDoc);
    assertEquals(expectedDoc.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocOfVariationDocDoesNotExist() {
    String variation = "projecta";
    when(getStorageManager().getCompleteVariation(TestConcreteDoc.class, "TEST000000000002", variation)).thenReturn(null);

    ClientResponse response = domainResource("testconcretedocs", "TEST000000000002").path(variation).header("Authorization", "bearer 12333322abef").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

}
