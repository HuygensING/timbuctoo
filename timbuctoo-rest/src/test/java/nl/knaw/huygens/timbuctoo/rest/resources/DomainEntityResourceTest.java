package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.REVISION_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.rest.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.rest.model.projecta.OtherDomainEntity;
import nl.knaw.huygens.timbuctoo.rest.model.projecta.ProjectADomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class DomainEntityResourceTest extends WebServiceTestSetup {

  private static final String PROJECTADOMAINENTITIES_RESOURCE = "projectadomainentities";
  private static final String BASEADOMAINENTITIES_RESOURCE = "basedomainentities";
  private static final String PERSISTENCE_PRODUCER = "persistenceProducer";
  private static final String INDEX_PRODUCER = "indexProducer";
  private static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  private static final Class<BaseDomainEntity> BASE_TYPE = BaseDomainEntity.class;
  private static final String DEFAULT_ID = "TEST000000000001";

  private WebResource createResource(String... pathElements) {
    WebResource resource = resource().path(Paths.DOMAIN_PREFIX);
    for (String pathElement : pathElements) {
      resource = resource.path(pathElement);
    }
    return resource;
  }

  @Before
  public void setupBroker() throws Exception {
    Broker broker = injector.getInstance(Broker.class);
    when(broker.getProducer(DomainEntityResource.INDEX_MSG_PRODUCER, Broker.INDEX_QUEUE)).thenReturn(getProducer(INDEX_PRODUCER));
    when(broker.getProducer(DomainEntityResource.PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE)).thenReturn(getProducer(PERSISTENCE_PRODUCER));
  }

  private Producer getProducer(String name) {
    return injector.getInstance(Key.get(Producer.class, Names.named(name)));
  }

  @SuppressWarnings("unchecked")
  private void whenJsonProviderReadFromThenReturn(Object value) throws Exception {
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(value);
  }

  @Test
  public void testGetDocExisting() {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityWithRelations(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(entity);

    ProjectADomainEntity actualDoc = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).get(ProjectADomainEntity.class);
    assertNotNull(actualDoc);
    assertEquals(entity.getId(), actualDoc.getId());
  }

  @Test
  public void testGetDocWithRevision() {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    int revision = 1;
    when(repository.getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision)).thenReturn(entity);

    ProjectADomainEntity actualDoc = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).queryParam(REVISION_KEY, "1").get(ProjectADomainEntity.class);
    assertNotNull(actualDoc);
    assertEquals(entity.getId(), actualDoc.getId());
    verify(repository).getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision);
  }

  @Test
  public void testGetDocWithRevisionTwo() {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    int revision = 2;
    when(repository.getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision)).thenReturn(entity);

    ProjectADomainEntity actualDoc = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).queryParam(REVISION_KEY, "2").get(ProjectADomainEntity.class);
    assertNotNull(actualDoc);
    assertEquals(entity.getId(), actualDoc.getId());
    verify(repository).getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision);
  }

  @Test
  public void testGetDocWithRevisionZero() {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    int revision = 0;
    when(repository.getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision)).thenReturn(entity);

    ProjectADomainEntity actualDoc = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).queryParam(REVISION_KEY, "0").get(ProjectADomainEntity.class);
    assertNotNull(actualDoc);
    assertEquals(entity.getId(), actualDoc.getId());
    verify(repository).getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision);
  }

  @Test
  public void testGetDocNonExistingInstance() {
    when(repository.getEntity(ProjectADomainEntity.class, "TST0000000001")).thenReturn(null);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, "TST0000000001").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocNonExistingClass() {
    ClientResponse response = createResource("unknown", "TST0000000001").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetEntities() {
    List<ProjectADomainEntity> expectedList = Lists.newArrayList(new ProjectADomainEntity("TEST001"), new ProjectADomainEntity("TEST002"));
    StorageIterator<ProjectADomainEntity> iterator = StorageIteratorStub.newInstance(expectedList);
    when(repository.getEntities(ProjectADomainEntity.class)).thenReturn(iterator);

    GenericType<List<ProjectADomainEntity>> genericType = new GenericType<List<ProjectADomainEntity>>() {};
    List<ProjectADomainEntity> actualList = createResource(PROJECTADOMAINENTITIES_RESOURCE).get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testPutAsUser() throws Exception {
    testPut(USER_ROLE);
  }

  @Test
  public void testPutAsAdmin() throws Exception {
    testPut(ADMIN_ROLE);
  }

  private void testPut(String userRole) throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, userRole);

    VRE vre = mock(VRE.class);
    when(vre.inScope(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntity(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);
    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), times(1)).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testPutItemNotInScope() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntity(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);
    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutExistingDocumentWithoutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntity(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutInvalidDocument() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    Validator validator = injector.getInstance(Validator.class);
    // Mockito could not mock the ConstraintViolation, it entered an infinit loop.
    ConstraintViolation<ProjectADomainEntity> violation = new ConstraintViolation<ProjectADomainEntity>() {

      @Override
      public String getMessage() {
        return null;
      }

      @Override
      public String getMessageTemplate() {
        return null;
      }

      @Override
      public ProjectADomainEntity getRootBean() {
        return null;
      }

      @Override
      public Class<ProjectADomainEntity> getRootBeanClass() {
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

    when(validator.validate(entity)).thenReturn(Sets.<ConstraintViolation<ProjectADomainEntity>> newHashSet(violation));

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutNonExistingDocument() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(ProjectADomainEntity.class, VRE_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    String id = "NULL000000000001";
    ProjectADomainEntity entity = new ProjectADomainEntity(id);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, id).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutNonExistingType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(ProjectADomainEntity.class, VRE_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("unknown", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutWrongType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(OtherDomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutOnSuperClass() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(OtherDomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutOnCollection() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostAsUser() throws Exception {
    testPost(USER_ROLE);
  }

  @Test
  public void testPostAsAdmin() throws Exception {
    testPost(ADMIN_ROLE);
  }

  private void testPost(String userRole) throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, userRole);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    when(repository.addDomainEntity(Matchers.<Class<ProjectADomainEntity>> any(), any(ProjectADomainEntity.class), any(Change.class))).thenReturn(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .post(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    String location = response.getHeaders().getFirst("Location");
    assertThat(location, containsString(Paths.DOMAIN_PREFIX + "/projectadomainentities/" + DEFAULT_ID));
    verify(getProducer(PERSISTENCE_PRODUCER), times(1)).send(ActionType.ADD, DEFAULT_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.ADD, DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testPostCollectionNotInScope() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    when(repository.addDomainEntity(Matchers.<Class<ProjectADomainEntity>> any(), any(ProjectADomainEntity.class), any(Change.class))).thenReturn(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .post(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  //Request handled by the framework.
  @Test
  public void testPostNonExistingCollection() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");

    ClientResponse response = createResource("unknown", "all").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .post(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostWrongType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VREManager vreManager = injector.getInstance(VREManager.class);
    when(vreManager.doesVREExist(VRE_ID)).thenReturn(true);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource("otherdomainentities").type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .post(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostSpecificDocument() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");

    ClientResponse response = createResource("otherentitys", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .post(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteAsUser() throws Exception {
    testDelete(USER_ROLE);
  }

  @Test
  public void testDeleteAsAdmin() throws Exception {
    testDelete(ADMIN_ROLE);
  }

  private void testDelete(String userRole) throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, userRole);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    when(repository.getEntity(BASE_TYPE, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(BASEADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(ActionType.DEL, BASE_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.DEL, BASE_TYPE, DEFAULT_ID);
  }

  @Test
  public void testDeleteProjectSpecificType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
    verify(repository, times(0)).getEntity(DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testDeleteDocumentWithoutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);
    when(repository.getEntity(BaseDomainEntity.class, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(BASEADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteDocumentDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VREManager vreManager = injector.getInstance(VREManager.class);
    when(vreManager.doesVREExist(VRE_ID)).thenReturn(true);

    when(repository.getEntity(BASE_TYPE, DEFAULT_ID)).thenReturn(null);

    ClientResponse response = createResource(BASEADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteTypeDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VREManager vreManager = injector.getInstance(VREManager.class);
    when(vreManager.doesVREExist(VRE_ID)).thenReturn(true);

    ClientResponse response = createResource("unknownEntities", DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteCollection() throws Exception {
    ClientResponse response = createResource(BASEADOMAINENTITIES_RESOURCE).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER), repository);
  }

  // Security tests

  @Test
  public void testGetDocNotLoggedIn() {
    ProjectADomainEntity expectedDoc = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityWithRelations(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(expectedDoc);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testGetDocEmptyAuthorizationKey() {
    ProjectADomainEntity expectedDoc = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityWithRelations(ProjectADomainEntity.class, DEFAULT_ID)).thenReturn(expectedDoc);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutUserNotLoggedIn() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    setUserNotLoggedIn();

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE, DEFAULT_ID).header(VRE_ID_KEY, VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE).header(VRE_ID_KEY, VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostUserNotLoggedIn() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    setUserNotLoggedIn();

    ClientResponse response = createResource(PROJECTADOMAINENTITIES_RESOURCE).header(VRE_ID_KEY, VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, entity);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteNotLoggedIn() throws Exception {
    setUserNotLoggedIn();

    ClientResponse response = createResource(BASEADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ClientResponse response = createResource(BASEADOMAINENTITIES_RESOURCE, DEFAULT_ID).type(MediaType.APPLICATION_JSON_TYPE).header(VRE_ID_KEY, VRE_ID).header("Authorization", AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  // Test put PID.

  @Test
  public void testPutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BaseDomainEntity.class)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    Class<ProjectADomainEntity> type = ProjectADomainEntity.class;
    String id1 = "ID1";
    String id2 = "ID2";
    String id3 = "ID3";

    when(repository.getAllIdsWithoutPID(type)).thenReturn(Lists.newArrayList(id1, id2, id3));

    ClientResponse response = doPutPIDRequest(PROJECTADOMAINENTITIES_RESOURCE);

    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
    verify(getProducer(PERSISTENCE_PRODUCER)).send(ActionType.MOD, type, id1);
    verify(getProducer(PERSISTENCE_PRODUCER)).send(ActionType.MOD, type, id2);
    verify(getProducer(PERSISTENCE_PRODUCER)).send(ActionType.MOD, type, id3);
    verifyZeroInteractions(getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutPIDOnBaseEntity() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BaseDomainEntity.class)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = doPutPIDRequest("basedomainentities");

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());

    verifyZeroInteractions(getProducer(INDEX_PRODUCER), getProducer(PERSISTENCE_PRODUCER));
    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>> any());
  }

  @Test
  public void testPutPIDBaseClassNotInScope() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BaseDomainEntity.class)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = doPutPIDRequest(PROJECTADOMAINENTITIES_RESOURCE);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());

    verifyZeroInteractions(getProducer(INDEX_PRODUCER), getProducer(PERSISTENCE_PRODUCER));
    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>> any());
  }

  @Test
  public void testPutPIDTypeDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VREManager vreManager = injector.getInstance(VREManager.class);
    when(vreManager.doesVREExist(VRE_ID)).thenReturn(true);

    ClientResponse response = doPutPIDRequest("unknowntypes");

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());

    verifyZeroInteractions(getProducer(INDEX_PRODUCER), getProducer(PERSISTENCE_PRODUCER));
    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>> any());
  }

  @Test
  public void testPutPIDUserNotAllowed() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VREManager vreManager = injector.getInstance(VREManager.class);
    when(vreManager.doesVREExist(VRE_ID)).thenReturn(true);

    ClientResponse response = doPutPIDRequest("unknowntypes");

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());

    verifyZeroInteractions(getProducer(INDEX_PRODUCER), getProducer(PERSISTENCE_PRODUCER));
    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>> any());
  }

  private ClientResponse doPutPIDRequest(String collectionName) {
    return createResource(collectionName, "pid").header(VRE_ID_KEY, VRE_ID).header("Authorization", AUTHORIZATION).put(ClientResponse.class);
  }

}
