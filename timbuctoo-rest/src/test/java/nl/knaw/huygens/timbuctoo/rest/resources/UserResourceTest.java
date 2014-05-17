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
import static org.mockito.Mockito.times;
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
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class UserResourceTest extends WebServiceTestSetup {

  private static final String OTHER_VRE_ID = "OtherVREId";
  private static final String DEFAULT_AUTHORIZATION = "bearer 12333322abef";
  private static final String AUTHORIZATION_KEY = "Authorization";
  private static final String VREAUTHORIZATIONS_PATH = "vreauthorizations";
  private static final String USERS_RESOURCE = "/" + Paths.SYSTEM_PREFIX + "/users";
  private static final String OTHER_USER_ID = "USER000000002";

  private WebResource resource;

  @Before
  public void setupWebResource() {
    resource = resource().path(USERS_RESOURCE);
  }

  @Test
  public void testGetAllUsers() {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    setUpVREManager(VRE_ID, true);

    List<User> expectedList = Lists.newArrayList(createUser("test", "test"), createUser("test1", "test1"), createUser("test", "test"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getAllLimited(User.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<User>> genericType = new GenericType<List<User>>() {};
    List<User> actualList = resource.header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllUsersNonFound() {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);

    List<User> expectedList = Lists.newArrayList();
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getAllLimited(User.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<User>> genericType = new GenericType<List<User>>() {};
    List<User> actualList = resource.header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllUsersNotInRole() {
    setupUserWithRoles(VRE_ID, USER_ID);
    setUpVREManager(VRE_ID, true);

    ClientResponse response = resource.header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);
    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetAllUsersNotLoggedIn() {
    setUserNotLoggedIn();

    ClientResponse response = resource.get(ClientResponse.class);
    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testGetUserAsAdmin() {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    User actual = resource.path(USER_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(User.class);
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetUserNotFound() {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(null);

    ClientResponse response = resource.path(USER_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);
    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetMyUserDataAsAdmin() {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    User actual = resource.path("me").header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(User.class);
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetMyUserDataAsUser() {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);
    setUpVREManager(VRE_ID, true);

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    User actual = resource.path("me").header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(User.class);
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetMyUserDataAsNewUser() throws Exception {
    setupUser(USER_ID);
    setUpVREManager(VRE_ID, true);

    MailSender mailSender = injector.getInstance(MailSender.class);

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    final User admin = createUser("admin", "admin");
    admin.setEmail("admin@admin.com");
    String adminId = "USER00000000002";
    admin.setId(adminId);

    VREAuthorization adminExample = new VREAuthorization(VRE_ID, null, ADMIN_ROLE);
    VREAuthorization adminAuth = new VREAuthorization(VRE_ID, adminId, ADMIN_ROLE);

    when(storageManager.findEntity(VREAuthorization.class, adminExample)).thenReturn(adminAuth);
    when(storageManager.getEntity(User.class, adminId)).thenReturn(admin);

    User actual = resource.path("me").header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());

    verify(mailSender).sendMail(anyString(), anyString(), anyString());
  }

  @Test
  public void testGetUserAsUser() {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);

    ClientResponse response = resource.path(USER_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);
    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetUserNotLoggedIn() {
    setUserNotLoggedIn();

    ClientResponse response = resource.path(USER_ID).get(ClientResponse.class);
    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testPutUser() {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    MailSender sender = injector.getInstance(MailSender.class);

    User user = createUser(USER_ID, "firstName", "lastName");
    user.setEmail("test@test.com");

    User original = createUser(USER_ID, "test", "test");
    original.setEmail("test@test.com");

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(original);

    ClientResponse response = resource.path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, user);
    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
    verify(sender).sendMail(anyString(), anyString(), anyString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutUserUserNotFound() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);

    User user = createUser(USER_ID, "firstName", "lastName");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws StorageException {
        // only if the document version does not exist an StorageException is thrown.
        throw new StorageException();
      }
    }).when(storageManager).updateSystemEntity(any(Class.class), any(User.class));

    ClientResponse response = resource.path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, user);
    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotInRole() {
    setupUserWithRoles(VRE_ID, USER_ID);
    setUpVREManager(VRE_ID, true);

    User user = createUser(USER_ID, "firstName", "lastName");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(null);

    ClientResponse response = resource.path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, user);
    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotLoggedIn() {
    User user = createUser(USER_ID, "firstName", "lastName");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(null);
    setUpVREManager(VRE_ID, true);

    setUserNotLoggedIn();

    ClientResponse response = resource.path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, user);
    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUser() {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    ClientResponse response = resource.path(USER_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserUserNotFound() {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(null);

    ClientResponse response = resource.path(USER_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotInRole() {
    setupUserWithRoles(VRE_ID, USER_ID);
    setUpVREManager(VRE_ID, true);

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    ClientResponse response = resource.path(USER_ID).header(VRE_ID_KEY, VRE_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).delete(ClientResponse.class);
    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotLoggedIn() {
    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);
    setUserNotLoggedIn();

    ClientResponse response = resource.path(USER_ID).delete(ClientResponse.class);
    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testGetVREAuthorizationAsAdmin() {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization expected = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    StorageManager storageManager = getStorageManager();
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(expected);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);

    assertEquals(Status.OK, response.getClientResponseStatus());
    verify(storageManager, times(1)).findEntity(VREAuthorization.class, expected);
    assertEquals(expected, response.getEntity(VREAuthorization.class));
  }

  @Test
  public void testGetVREAuthorizationAsUser() {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);
    StorageManager storageManager = getStorageManager();
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(storageManager, never()).findEntity(VREAuthorization.class, example);
  }

  @Test
  public void testGetVREAuthorizationNotLoggedIn() {
    setUserNotLoggedIn();
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    
    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);

    verify(getStorageManager(), never()).findEntity(VREAuthorization.class, example);
    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testGetVREAuthorizationNotFound() {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);

    StorageManager storageManager = getStorageManager();
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(null);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
    verify(storageManager, times(1)).findEntity(VREAuthorization.class, example);
  }

  @Test
  public void testGetVREAuthorizationNotInScope() {
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(OTHER_VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, OTHER_VRE_ID)
        .get(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(getStorageManager(), never()).findEntity(VREAuthorization.class, example);
  }

  @Test
  public void testPostVREAuthorizationAsAdmin() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    StorageManager storageManager = getStorageManager();

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .post(ClientResponse.class, vreAuthorization);

    assertEquals(Status.CREATED, response.getClientResponseStatus());
    String location = response.getHeaders().getFirst("Location");
    assertThat(location, containsString(USERS_RESOURCE + "/" + USER_ID + "/" + VREAUTHORIZATIONS_PATH + "/" + VRE_ID));
    verify(storageManager).addSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPostVREAuthorizationAsUser() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(getStorageManager(), never()).addSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPostVREAuthorizationNotLoggedIn() throws Exception {
    setUserNotLoggedIn();
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
    verify(getStorageManager(), never()).addSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPostVREAuthorizationVREAuthorizationIsNull() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization vreAuthorization = null;
    StorageManager storageManager = getStorageManager();

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID)
        .post(ClientResponse.class, vreAuthorization);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(storageManager, never()).addSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPostVREAuthorizationNotInScope() throws Exception {
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(OTHER_VRE_ID, true);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    StorageManager storageManager = getStorageManager();

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, OTHER_VRE_ID).post(ClientResponse.class, vreAuthorization);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(storageManager, never()).addSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPutVREAuthorizationAsAdmin() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);

    StorageManager storageManager = getStorageManager();
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(vreAuthorization);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, vreAuthorization);

    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
    verify(storageManager).findEntity(VREAuthorization.class, example);
    verify(storageManager).updateSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPutVREAuthorizationAsUser() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, vreAuthorization);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    StorageManager storageManager = getStorageManager();
    verify(storageManager, never()).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).updateSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPutVREAuthorizationNotLoggedIn() throws Exception {
    setUserNotLoggedIn();
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, vreAuthorization);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
    StorageManager storageManager = getStorageManager();
    verify(storageManager, never()).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).updateSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPutVREAuthorizationVREAuthorizationIsNull() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization vreAuthorization = null;

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, vreAuthorization);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    StorageManager storageManager = getStorageManager();
    verify(storageManager, never()).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).updateSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPutVREAuthorizationNotFound() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, vreAuthorization);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
    StorageManager storageManager = getStorageManager();
    verify(storageManager).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).updateSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testPutVREAuthorizationNotInScope() throws Exception {
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(OTHER_VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    StorageManager storageManager = getStorageManager();
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(vreAuthorization);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, OTHER_VRE_ID).put(ClientResponse.class, vreAuthorization);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    verify(storageManager, never()).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).updateSystemEntity(VREAuthorization.class, vreAuthorization);
  }

  @Test
  public void testDeleteVREAuthorizationAsAdmin() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    StorageManager storageManager = getStorageManager();
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(vreAuthorization);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.NO_CONTENT, response.getClientResponseStatus());
    verify(storageManager).findEntity(VREAuthorization.class, example);
    verify(storageManager).deleteSystemEntity(vreAuthorization);
  }

  @Test
  public void testDeleteAuthorizationAsUser() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    StorageManager storageManager = getStorageManager();
    verify(storageManager, never()).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).deleteSystemEntity(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteVREAuthorizationNotLoggedIn() throws Exception {
    setUserNotLoggedIn();
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
    StorageManager storageManager = getStorageManager();
    verify(storageManager, never()).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).deleteSystemEntity(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteAuthorizationNotFound() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);

    StorageManager storageManager = getStorageManager();
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(null);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
    verify(storageManager).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).deleteSystemEntity(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteVREAuthorizationNotInScope() throws Exception {
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    setUpVREManager(OTHER_VRE_ID, true);
    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);

    ClientResponse response = resource.path(USER_ID).path(VREAUTHORIZATIONS_PATH).path(VRE_ID).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION)
        .header(VRE_ID_KEY, OTHER_VRE_ID).delete(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
    StorageManager storageManager = getStorageManager();
    verify(storageManager, never()).findEntity(VREAuthorization.class, example);
    verify(storageManager, never()).deleteSystemEntity(any(VREAuthorization.class));
  }

  @Test
  public void testGetRolesAsAdmin() {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    setUpVREManager(VRE_ID, true);

    ClientResponse response = resource.path("roles").header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);

    assertEquals(Status.OK, response.getClientResponseStatus());
    List<String> actual = response.getEntity(new GenericType<List<String>>() {});
    assertThat(actual, containsInAnyOrder(UserRoles.getAll().toArray()));
  }

  @Test
  public void testGetRolesAsUser() {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);
    setUpVREManager(VRE_ID, true);

    ClientResponse response = resource.path("roles").header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);

    assertEquals(Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetRolesNotLoggedIn() {
    setUserNotLoggedIn();
    setUpVREManager(VRE_ID, true);

    ClientResponse response = resource.path("roles").header(AUTHORIZATION_KEY, DEFAULT_AUTHORIZATION).header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);

    assertEquals(Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  private User createUser(String firstName, String lastName) {
    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }

  private User createUser(String id, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }

}
