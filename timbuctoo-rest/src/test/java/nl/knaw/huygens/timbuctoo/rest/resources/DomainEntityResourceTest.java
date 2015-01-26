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

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static nl.knaw.huygens.timbuctoo.config.TypeNames.getExternalName;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.REVISION_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
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
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import test.rest.model.BaseDomainEntity;
import test.rest.model.projecta.OtherDomainEntity;
import test.rest.model.projecta.ProjectADomainEntity;
import test.rest.model.projecta.ProjectARelation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class DomainEntityResourceTest extends WebServiceTestSetup {

  protected static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  protected static final String DEFAULT_RESOURCE = TypeNames.getExternalName(DEFAULT_TYPE);

  protected static final Class<BaseDomainEntity> BASE_TYPE = BaseDomainEntity.class;
  protected static final String BASE_RESOURCE = TypeNames.getExternalName(BASE_TYPE);

  protected static final String PERSISTENCE_PRODUCER = "persistenceProducer";
  protected static final String INDEX_PRODUCER = "indexProducer";
  protected static final String DEFAULT_ID = "TEST000000000001";

  protected WebResource createResource(String... pathElements) {
    return addPathToWebResource(resource().path(getAPIVersion()).path(Paths.DOMAIN_PREFIX), pathElements);
  }

  @Before
  public void setupBroker() throws Exception {
    Broker broker = injector.getInstance(Broker.class);
    when(broker.getProducer(DomainEntityResource.INDEX_MSG_PRODUCER, Broker.INDEX_QUEUE)).thenReturn(getProducer(INDEX_PRODUCER));
    when(broker.getProducer(DomainEntityResource.PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE)).thenReturn(getProducer(PERSISTENCE_PRODUCER));
  }

  protected Producer getProducer(String name) {
    return injector.getInstance(Key.get(Producer.class, Names.named(name)));
  }

  @SuppressWarnings("unchecked")
  protected void whenJsonProviderReadFromThenReturn(Object value) throws Exception {
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(value);
  }

  @Test
  public void testGetEntityExisting() {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityWithRelations(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    ProjectADomainEntity actualDoc = response.getEntity(DEFAULT_TYPE);
    assertNotNull(actualDoc);
    assertEquals(entity.getId(), actualDoc.getId());
  }

  @Test
  public void testGetEntityWithRevision() {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    int revision = 2;
    when(repository.getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision)).thenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .queryParam(REVISION_KEY, "2") //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    ProjectADomainEntity actualDoc = response.getEntity(DEFAULT_TYPE);

    assertNotNull(actualDoc);
    assertEquals(entity.getId(), actualDoc.getId());
    verify(repository).getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision);
  }

  @Test
  public void testGetEntityNonExistingInstance() {
    when(repository.getEntity(DEFAULT_TYPE, "TST0000000001")).thenReturn(null);

    ClientResponse response = createResource(DEFAULT_RESOURCE, "TST0000000001") //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Test
  public void testGetEntityNonExistingClass() {
    ClientResponse response = createResource("unknown", "TST0000000001") //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Test
  public void testGetEntities() {
    List<ProjectADomainEntity> expectedList = Lists.newArrayList(new ProjectADomainEntity("TEST001"), new ProjectADomainEntity("TEST002"));
    StorageIterator<ProjectADomainEntity> iterator = StorageIteratorStub.newInstance(expectedList);
    when(repository.getDomainEntities(DEFAULT_TYPE)).thenReturn(iterator);

    GenericType<List<ProjectADomainEntity>> genericType = new GenericType<List<ProjectADomainEntity>>() {};

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    List<ProjectADomainEntity> actualList = response.getEntity(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  // --- PUT -------------------------------------------------------------------

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
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntity(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NO_CONTENT);

    verify(getProducer(PERSISTENCE_PRODUCER), times(1)).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testPutItemNotInScope() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntity(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutExistingDocumentWithoutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntity(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutInvalidDocument() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    Validator validator = injector.getInstance(Validator.class);
    // Mockito could not mock the ConstraintViolation, it entered an infinite loop.
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

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutWhenRepositoryThrowsAnUpdateException() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntity(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    doThrow(UpdateException.class).when(repository).updateDomainEntity(Matchers.<Class<ProjectADomainEntity>> any(), any(ProjectADomainEntity.class), any(Change.class));
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.CONFLICT);

    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), never()).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testPutWhenRepositoryThrowsAnNoSuchEnityException() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntity(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    doThrow(NoSuchEntityException.class).when(repository).updateDomainEntity(Matchers.<Class<ProjectADomainEntity>> any(), any(ProjectADomainEntity.class), any(Change.class));
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), never()).send(ActionType.MOD, DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testPutNonExistingDocument() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, VRE_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    String id = "NULL000000000001";
    ProjectADomainEntity entity = new ProjectADomainEntity(id);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, id) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutNonExistingType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, VRE_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("unknown", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutWrongType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(OtherDomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutOnSuperClass() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(OtherDomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutOnCollection() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities") //
        .type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION, CREDENTIALS).header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.METHOD_NOT_ALLOWED);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutRelation() throws Exception {
    Class<ProjectARelation> type = ProjectARelation.class;
    String id = "RELA000000000001";
    String sourceType = TypeNames.getInternalName(BASE_TYPE);
    String sourceId = "TEST000000000001";
    String targetType = TypeNames.getInternalName(BASE_TYPE);
    String targetId = "TEST000000000002";

    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(type, id)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectARelation entity = RelationBuilder.newInstance(type) //
        .withId(id) //
        .withSourceType(sourceType) //
        .withSourceId(sourceId) //
        .withTargetType(targetType) //
        .withTargetId(targetId) //
        .withPid("65262031-c5c2-44f9-b90e-11f9fc7736cf") //
        .build();
    when(repository.getEntity(type, id)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(getExternalName(type), id) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NO_CONTENT);

    // handling of the relation entity itself
    verify(getProducer(PERSISTENCE_PRODUCER), times(1)).send(ActionType.MOD, type, id);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.MOD, type, id);
    // handling of source and target entities of the relation
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.MOD, BASE_TYPE, sourceId);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.MOD, BASE_TYPE, targetId);
  }

  // --- POST ------------------------------------------------------------------

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
    when(repository.addDomainEntity(Matchers.<Class<ProjectADomainEntity>> any(), any(DEFAULT_TYPE), any(Change.class))).thenReturn(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.CREATED);

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
    when(repository.addDomainEntity(Matchers.<Class<ProjectADomainEntity>> any(), any(DEFAULT_TYPE), any(Change.class))).thenReturn(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  //Request handled by the framework.
  @Test
  public void testPostNonExistingCollection() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");

    ClientResponse response = createResource("unknown", "all") //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostWrongType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    when(repository.doesVREExist(VRE_ID)).thenReturn(true);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource("otherdomainentities") //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostSpecificDocument() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");

    ClientResponse response = createResource("otherentitys", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.METHOD_NOT_ALLOWED);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostRelation() throws Exception {
    Class<ProjectARelation> type = ProjectARelation.class;
    String id = "RELA000000000001";
    String sourceType = TypeNames.getInternalName(BASE_TYPE);
    String sourceId = "TEST000000000001";
    String targetType = TypeNames.getInternalName(BASE_TYPE);
    String targetId = "TEST000000000002";

    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(type)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectARelation entity = RelationBuilder.newInstance(type) //
        .withId(id) //
        .withSourceType(sourceType) //
        .withSourceId(sourceId) //
        .withTargetType(targetType) //
        .withTargetId(targetId) //
        .build();
    when(repository.addDomainEntity(Matchers.<Class<ProjectARelation>> any(), any(type), any(Change.class))).thenReturn(id);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(getExternalName(type)) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID) //
        .post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.CREATED);

    String path = Paths.DOMAIN_PREFIX + "/" + getExternalName(type) + "/" + id;
    assertThat(response.getHeaders().getFirst("Location"), containsString(path));
    // handling of the relation entity itself
    verify(getProducer(PERSISTENCE_PRODUCER), times(1)).send(ActionType.ADD, type, id);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.ADD, type, id);
    // handling of source and target entities of the relation
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.MOD, BASE_TYPE, sourceId);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.MOD, BASE_TYPE, targetId);
  }

  // ---------------------------------------------------------------------------

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

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NO_CONTENT);

    verify(getProducer(PERSISTENCE_PRODUCER), never()).send(ActionType.DEL, BASE_TYPE, DEFAULT_ID);
    verify(getProducer(INDEX_PRODUCER), times(1)).send(ActionType.DEL, BASE_TYPE, DEFAULT_ID);
  }

  @Test
  public void testDeleteProjectSpecificType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
    verify(repository, times(0)).getEntity(DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testDeleteDocumentWithoutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);
    when(repository.getEntity(BASE_TYPE, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteEntityThatDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    when(repository.doesVREExist(VRE_ID)).thenReturn(true);
    when(repository.getEntity(BASE_TYPE, DEFAULT_ID)).thenReturn(null);

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteTypeDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    when(repository.doesVREExist(VRE_ID)).thenReturn(true);

    ClientResponse response = createResource("unknownEntities", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteCollection() throws Exception {
    ClientResponse response = createResource(BASE_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.METHOD_NOT_ALLOWED);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER), repository);
  }

  // Security tests

  @Test
  public void testGetEntityNotLoggedIn() {
    ProjectADomainEntity expectedDoc = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityWithRelations(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(expectedDoc);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);
  }

  @Test
  public void testGetEntityEmptyAuthorizationKey() {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityWithRelations(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);
  }

  @Test
  public void testPutUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutUserNotLoggedIn() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    setUserNotLoggedIn();

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(VRE_ID_KEY, VRE_ID) //
        .put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS).header(VRE_ID_KEY, VRE_ID) //
        .post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPostUserNotLoggedIn() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    setUserNotLoggedIn();

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(VRE_ID_KEY, VRE_ID) //
        .post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteNotLoggedIn() throws Exception {
    setUserNotLoggedIn();

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(VRE_ID_KEY, VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testDeleteUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS).header(VRE_ID_KEY, VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(PERSISTENCE_PRODUCER), getProducer(INDEX_PRODUCER));
  }

  // Test put PID.

  @Test
  public void testPutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    String id1 = "ID1";
    String id2 = "ID2";
    String id3 = "ID3";

    when(repository.getAllIdsWithoutPID(DEFAULT_TYPE)).thenReturn(Lists.newArrayList(id1, id2, id3));

    ClientResponse response = doPutPIDRequest(DEFAULT_RESOURCE);
    verifyResponseStatus(response, Status.NO_CONTENT);

    verify(getProducer(PERSISTENCE_PRODUCER)).send(ActionType.MOD, DEFAULT_TYPE, id1);
    verify(getProducer(PERSISTENCE_PRODUCER)).send(ActionType.MOD, DEFAULT_TYPE, id2);
    verify(getProducer(PERSISTENCE_PRODUCER)).send(ActionType.MOD, DEFAULT_TYPE, id3);
    verifyZeroInteractions(getProducer(INDEX_PRODUCER));
  }

  @Test
  public void testPutPIDOnBaseEntity() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = doPutPIDRequest(BASE_RESOURCE);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getProducer(INDEX_PRODUCER), getProducer(PERSISTENCE_PRODUCER));
    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>> any());
  }

  @Test
  public void testPutPIDBaseClassNotInScope() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = doPutPIDRequest(DEFAULT_RESOURCE);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(INDEX_PRODUCER), getProducer(PERSISTENCE_PRODUCER));
    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>> any());
  }

  @Test
  public void testPutPIDTypeDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    when(repository.doesVREExist(VRE_ID)).thenReturn(true);

    ClientResponse response = doPutPIDRequest("unknowntypes");
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getProducer(INDEX_PRODUCER), getProducer(PERSISTENCE_PRODUCER));
    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>> any());
  }

  @Test
  public void testPutPIDUserNotAllowed() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    when(repository.doesVREExist(VRE_ID)).thenReturn(true);

    ClientResponse response = doPutPIDRequest("unknowntypes");
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getProducer(INDEX_PRODUCER), getProducer(PERSISTENCE_PRODUCER));
    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>> any());
  }

  private ClientResponse doPutPIDRequest(String collectionName) {
    return createResource(collectionName, "pid") //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID) //
        .put(ClientResponse.class);
  }

}
