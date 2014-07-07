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

import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.security.UserRoles;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class UserResourceTest extends WebServiceTestSetup {

  private static final String OTHER_VRE_ID = "OtherVREId";
  private static final String VREAUTHORIZATIONS_PATH = "vreauthorizations";
  private static final String OTHER_USER_ID = "USER000000002";
  private static final String[] NO_ROLES = new String[0];

  private WebResource createResource(String version, String... pathElements) {
    WebResource resource = resource();

    if (version != null) {
      resource = resource.path(version);
    }

    resource = resource.path(Paths.SYSTEM_PREFIX).path("users");
    for (String pathElement : pathElements) {
      resource = resource.path(pathElement);
    }
    return resource;
  }

  @Test
  public void testGetAllUsers() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    StorageIterator<User> iterator = StorageIteratorStub.newInstance(createUser("id1", "a", "b"), createUser("id2", "c", "d"));
    when(repository.getSystemEntities(User.class)).thenReturn(iterator);

    GenericType<List<User>> genericType = new GenericType<List<User>>() {};
    WebResource resource = createResource(null);
    List<User> users = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(genericType);

    assertEquals(2, users.size());
  }

  @Test
  public void testGetAllUsersNotInRole() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, NO_ROLES);

    WebResource resource = createResource(null);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetAllUsersNotLoggedIn() {
    setUserNotLoggedIn();

    WebResource resource = createResource(null);
    ClientResponse response = resource.get(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testGetUserAsAdmin() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    User expected = createUser(USER_ID, "test", "test");
    when(repository.getEntity(User.class, USER_ID)).thenReturn(expected);

    WebResource resource = createResource(null, USER_ID);
    User actual = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetUserNotFound() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    when(repository.getEntity(User.class, USER_ID)).thenReturn(null);

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetMyUserDataAsAdmin() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    User expected = createUser(USER_ID, "test", "test");
    when(repository.getEntity(User.class, USER_ID)).thenReturn(expected);

    WebResource resource = createResource(null, "me");
    User actual = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetMyUserDataAsUser() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    User expected = createUser(USER_ID, "test", "test");
    when(repository.getEntity(User.class, USER_ID)).thenReturn(expected);

    WebResource resource = createResource(null, "me");
    User actual = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetMyUserDataAsNewUser() throws Exception {
    setUpVREManager(VRE_ID, true);
    User user = new User();
    user.setId(USER_ID);
    when(repository.findEntity(User.class, user)).thenReturn(user);

    MailSender mailSender = injector.getInstance(MailSender.class);

    User expected = createUser(USER_ID, "test", "test");
    when(repository.getEntity(User.class, USER_ID)).thenReturn(expected);

    String adminId = "USER00000000002";
    User admin = createUser(adminId, "admin", "admin");
    admin.setEmail("admin@admin.com");
    admin.setId(adminId);

    VREAuthorization adminExample = new VREAuthorization(VRE_ID, null, ADMIN_ROLE);
    VREAuthorization adminAuth = new VREAuthorization(VRE_ID, adminId, ADMIN_ROLE);

    when(repository.findEntity(VREAuthorization.class, adminExample)).thenReturn(adminAuth);
    when(repository.getEntity(User.class, adminId)).thenReturn(admin);

    WebResource resource = createResource(null, "me");
    User actual = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
    verify(mailSender).sendMail(anyString(), anyString(), anyString());
  }

  @Test
  public void testGetUserAsUser() {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetUserNotLoggedIn() {
    setUserNotLoggedIn();

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.get(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testPutUser() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    MailSender sender = injector.getInstance(MailSender.class);

    User user = createUser(USER_ID, "firstName", "lastName");
    user.setEmail("test@test.com");

    User original = createUser(USER_ID, "test", "test");
    original.setEmail("test@test.com");

    when(repository.getEntity(User.class, USER_ID)).thenReturn(original);

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).put(ClientResponse.class, user);

    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
    verify(sender).sendMail(anyString(), anyString(), anyString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutUserUserNotFound() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    User user = createUser(USER_ID, "firstName", "lastName");
    doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws StorageException {
        // only if the document version does not exist an StorageException is thrown.
        throw new StorageException();
      }
    }).when(repository).updateSystemEntity(any(Class.class), any(User.class));

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).put(ClientResponse.class, user);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotInRole() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, NO_ROLES);

    User user = createUser(USER_ID, "firstName", "lastName");
    when(repository.getEntity(User.class, USER_ID)).thenReturn(null);

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).put(ClientResponse.class, user);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotLoggedIn() {
    setUpVREManager(VRE_ID, true);
    User user = createUser(USER_ID, "firstName", "lastName");
    when(repository.getEntity(User.class, USER_ID)).thenReturn(null);

    setUserNotLoggedIn();

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, user);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUser() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserUserNotFound() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    when(repository.getEntity(User.class, USER_ID)).thenReturn(null);

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotInRole() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, NO_ROLES);

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.header("VRE_ID", VRE_ID).header("Authorization", AUTHORIZATION).delete(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotLoggedIn() {
    setupUserWithRoles(VRE_ID, USER_ID, NO_ROLES);
    setUserNotLoggedIn();

    WebResource resource = createResource(null, USER_ID);
    ClientResponse response = resource.delete(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testGetVREAuthorizationAsAdmin() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization expected = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(repository.findEntity(VREAuthorization.class, example)).thenReturn(expected);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.OK, response.getClientResponseStatus());
    assertEquals(expected, response.getEntity(VREAuthorization.class));
  }

  @Test
  public void testGetVREAuthorizationAsUser() {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetVREAuthorizationNotLoggedIn() {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);
    setUserNotLoggedIn();

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testGetVREAuthorizationNotFound() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    when(repository.findEntity(VREAuthorization.class, example)).thenReturn(null);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetVREAuthorizationNotInScope() {
    setUpVREManager(OTHER_VRE_ID, true);
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", OTHER_VRE_ID).get(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testPostVREAuthorizationAsAdmin() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH);
    String uri = resource.getURI().toString();
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).post(ClientResponse.class, authorization);

    assertEquals(Status.CREATED, response.getClientResponseStatus());
    String location = response.getHeaders().getFirst("Location");
    assertThat(location, containsString(uri));
    verify(repository).addSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPostVREAuthorizationAsUser() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);

    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).post(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPostVREAuthorizationNotLoggedIn() throws Exception {
    setUserNotLoggedIn();

    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH);
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).post(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPostVREAuthorizationVREAuthorizationIsNull() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization authorization = null;
    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).post(ClientResponse.class, authorization);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPostVREAuthorizationNotInScope() throws Exception {
    setUpVREManager(OTHER_VRE_ID, true);
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", OTHER_VRE_ID).post(ClientResponse.class, authorization);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPutVREAuthorizationAsAdmin() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(repository.findEntity(VREAuthorization.class, example)).thenReturn(authorization);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).put(ClientResponse.class, authorization);

    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
    verify(repository).updateSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPutVREAuthorizationAsUser() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);

    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).put(ClientResponse.class, authorization);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(repository, never()).updateSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPutVREAuthorizationNotLoggedIn() throws Exception {
    setUpVREManager(VRE_ID, true);
    setUserNotLoggedIn();

    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).put(ClientResponse.class, authorization);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
    verify(repository, never()).updateSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPutVREAuthorizationVREAuthorizationIsNull() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization authorization = null;
    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).put(ClientResponse.class, authorization);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(repository, never()).updateSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPutVREAuthorizationNotFound() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).put(ClientResponse.class, authorization);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
    verify(repository, never()).updateSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testPutVREAuthorizationNotInScope() throws Exception {
    setUpVREManager(OTHER_VRE_ID, true);
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(repository.findEntity(VREAuthorization.class, example)).thenReturn(authorization);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", OTHER_VRE_ID).put(ClientResponse.class, authorization);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(repository, never()).updateSystemEntity(VREAuthorization.class, authorization);
  }

  @Test
  public void testDeleteVREAuthorizationAsAdmin() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(repository.findEntity(VREAuthorization.class, example)).thenReturn(authorization);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
    verify(repository).deleteSystemEntity(authorization);
  }

  @Test
  public void testDeleteAuthorizationAsUser() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(repository, never()).deleteSystemEntity(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteVREAuthorizationNotLoggedIn() throws Exception {
    setUpVREManager(VRE_ID, true);
    setUserNotLoggedIn();

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
    verify(repository, never()).deleteSystemEntity(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteAuthorizationNotFound() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    when(repository.findEntity(VREAuthorization.class, example)).thenReturn(null);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
    verify(repository, never()).deleteSystemEntity(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteVREAuthorizationNotInScope() throws Exception {
    setUpVREManager(OTHER_VRE_ID, true);
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    WebResource resource = createResource(null, USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", AUTHORIZATION).header("VRE_ID", OTHER_VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(repository, never()).deleteSystemEntity(any(VREAuthorization.class));
  }

  @Test
  public void testGetRolesAsAdmin() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    WebResource resource = createResource(null, "roles");
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.OK, response.getClientResponseStatus());
    List<String> actual = response.getEntity(new GenericType<List<String>>() {});
    assertThat(actual, containsInAnyOrder(UserRoles.getAll().toArray()));
  }

  @Test
  public void testGetRolesAsUser() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    WebResource resource = createResource(null, "roles");
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetRolesNotLoggedIn() {
    setUpVREManager(VRE_ID, true);
    setUserNotLoggedIn();

    WebResource resource = createResource(null, "roles");
    ClientResponse response = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  private User createUser(String id, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }

  /************************************************************************************************
   * V1 Test
   ***********************************************************************************************/
  @Test
  public void testGetMyUserDataAsUserV1() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    User expected = createUser(USER_ID, "test", "test");
    when(repository.getEntity(User.class, USER_ID)).thenReturn(expected);

    WebResource resource = createResource(Paths.V1_PATH, "me");
    User actual = resource.header("Authorization", AUTHORIZATION).header("VRE_ID", VRE_ID).get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

}
