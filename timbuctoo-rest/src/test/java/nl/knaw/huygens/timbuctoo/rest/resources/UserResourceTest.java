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
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
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

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class UserResourceTest extends WebServiceTestSetup {

  private static final String OTHER_VRE_ID = "OtherVREId";
  protected static final String VREAUTHORIZATIONS_PATH = "vreauthorizations";
  protected static final String OTHER_USER_ID = "USER000000002";
  private static final String[] NO_ROLES = new String[0];

  protected WebResource createResource(String... pathElements) {
    WebResource resource = resource();

    resource = resource.path(getAPIVersion());

    resource = resource.path(Paths.SYSTEM_PREFIX).path("users");
    for (String pathElement : pathElements) {
      resource = resource.path(pathElement);
    }
    return resource;
  }

  @Override
  protected String getAPIVersion() {
    return "";
  }

  @Test
  public void testGetAllUsers() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    StorageIterator<User> iterator = StorageIteratorStub.newInstance(createUser("id1", "a", "b"), createUser("id2", "c", "d"));
    when(userConfigurationHandler.getUsers()).thenReturn(iterator);

    GenericType<List<User>> genericType = new GenericType<List<User>>() {};
    List<User> users = createResource() //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(genericType);

    assertEquals(2, users.size());
  }

  @Test
  public void testGetAllUsersNotInRole() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, NO_ROLES);

    ClientResponse response = createResource() //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);
  }

  @Test
  public void testGetAllUsersNotLoggedIn() {
    setUserNotLoggedIn();

    ClientResponse response = createResource() //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);
  }

  @Test
  public void testGetUserAsAdmin() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    User expected = createUser(USER_ID, "test", "test");
    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(expected);

    User actual = createResource(USER_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetUserNotFound() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(null);

    ClientResponse response = createResource(USER_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Test
  public void testGetMyUserDataAsAdmin() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    User expected = createUser(USER_ID, "test", "test");
    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(expected);

    User actual = createResource("me") //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetMyUserDataAsUser() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    User expected = createUser(USER_ID, "test", "test");
    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(expected);

    User actual = createResource("me") //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetMyUserDataAsNewUser() throws Exception {
    setUpVREManager(VRE_ID, true);
    User user = new User();
    user.setId(USER_ID);
    when(userConfigurationHandler.findUser(user)).thenReturn(user);

    MailSender mailSender = injector.getInstance(MailSender.class);

    User expected = createUser(USER_ID, "test", "test");
    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(expected);

    String adminId = "USER00000000002";
    User admin = createUser(adminId, "admin", "admin");
    admin.setEmail("admin@admin.com");
    admin.setId(adminId);

    VREAuthorization adminExample = new VREAuthorization(VRE_ID, null, ADMIN_ROLE);
    VREAuthorization adminAuth = new VREAuthorization(VRE_ID, adminId, ADMIN_ROLE);

    when(userConfigurationHandler.findVREAuthorization(adminExample)).thenReturn(adminAuth);
    when(userConfigurationHandler.getUser(adminId)).thenReturn(admin);

    ClientResponse response = createResource("me") //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    User actual = response.getEntity(User.class);
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
    verify(mailSender).sendMail(anyString(), anyString(), anyString());
  }

  @Test
  public void testGetUserAsUser() {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);

    ClientResponse response = createResource(USER_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);
  }

  @Test
  public void testGetUserNotLoggedIn() {
    setUserNotLoggedIn();

    ClientResponse response = createResource(USER_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);
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

    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(original);

    ClientResponse response = createResource(USER_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, user);
    verifyResponseStatus(response, Status.NO_CONTENT);

    verify(sender).sendMail(anyString(), anyString(), anyString());
  }

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
    }).when(userConfigurationHandler).updateUser(any(User.class));

    ClientResponse response = createResource(USER_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, user);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Test
  public void testPutUserNotInRole() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, NO_ROLES);

    User user = createUser(USER_ID, "firstName", "lastName");
    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(null);

    ClientResponse response = createResource(USER_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, user);
    verifyResponseStatus(response, Status.FORBIDDEN);
  }

  @Test
  public void testPutUserNotLoggedIn() {
    setUpVREManager(VRE_ID, true);
    User user = createUser(USER_ID, "firstName", "lastName");
    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(null);

    setUserNotLoggedIn();

    ClientResponse response = createResource(USER_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .put(ClientResponse.class, user);
    verifyResponseStatus(response, Status.UNAUTHORIZED);
  }

  @Ignore("Re enable the test if we have rethought the concept of deleting a user.")
  @Test
  public void testDeleteUser() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    ClientResponse response = createResource(USER_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NO_CONTENT);
  }

  @Ignore("Re enable the test if we have rethought the concept of deleting a user.")
  @Test
  public void testDeleteUserUserNotFound() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);
    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(null);

    ClientResponse response = createResource(USER_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Ignore("Re enable the test if we have rethought the concept of deleting a user.")
  @Test
  public void testDeleteUserNotInRole() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, NO_ROLES);

    ClientResponse response = createResource(USER_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);
  }

  @Ignore("Re enable the test if we have rethought the concept of deleting a user.")
  @Test
  public void testDeleteUserNotLoggedIn() {
    setupUserWithRoles(VRE_ID, USER_ID, NO_ROLES);
    setUserNotLoggedIn();

    ClientResponse response = createResource(USER_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);
  }

  @Test
  public void testGetVREAuthorizationAsAdmin() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization expected = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(userConfigurationHandler.findVREAuthorization(example)).thenReturn(expected);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    assertEquals(expected, response.getEntity(VREAuthorization.class));
  }

  @Test
  public void testGetVREAuthorizationAsUser() {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);
  }

  @Test
  public void testGetVREAuthorizationNotLoggedIn() {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);
    setUserNotLoggedIn();

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);
  }

  @Test
  public void testGetVREAuthorizationNotFound() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    when(userConfigurationHandler.findVREAuthorization(example)).thenReturn(null);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Test
  public void testGetVREAuthorizationNotInScope() {
    setUpVREManager(OTHER_VRE_ID, true);
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", OTHER_VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);
  }

  @Test
  public void testPostVREAuthorizationAsAdmin() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    WebResource resource = createResource(USER_ID, VREAUTHORIZATIONS_PATH);
    String uri = resource.getURI().toString();
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .post(ClientResponse.class, authorization);
    verifyResponseStatus(response, Status.CREATED);

    String location = response.getHeaders().getFirst("Location");
    assertThat(location, containsString(uri));
    verify(userConfigurationHandler).addVREAuthorization(authorization);
  }

  @Test
  public void testPostVREAuthorizationAsUser() throws Exception {
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .post(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verify(userConfigurationHandler, never()).addVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testPostVREAuthorizationNotLoggedIn() throws Exception {
    setUserNotLoggedIn();

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .post(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verify(userConfigurationHandler, never()).addVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testPostVREAuthorizationVREAuthorizationIsNull() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .post(ClientResponse.class, null);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verify(userConfigurationHandler, never()).addVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testPostVREAuthorizationNotInScope() throws Exception {
    setUpVREManager(OTHER_VRE_ID, true);
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", OTHER_VRE_ID) //
        .post(ClientResponse.class, authorization);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verify(userConfigurationHandler, never()).addVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testPutVREAuthorizationAsAdmin() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(userConfigurationHandler.findVREAuthorization(example)).thenReturn(authorization);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, authorization);
    verifyResponseStatus(response, Status.NO_CONTENT);

    verify(userConfigurationHandler).updateVREAuthorization(authorization);
  }

  @Test
  public void testPutVREAuthorizationAsUser() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, authorization);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verify(userConfigurationHandler, never()).updateVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testPutVREAuthorizationNotLoggedIn() throws Exception {
    setUpVREManager(VRE_ID, true);
    setUserNotLoggedIn();
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, authorization);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verify(userConfigurationHandler, never()).updateVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testPutVREAuthorizationVREAuthorizationIsNull() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, null);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verify(userConfigurationHandler, never()).updateVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testPutVREAuthorizationNotFound() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, authorization);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verify(userConfigurationHandler, never()).updateVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testPutVREAuthorizationNotInScope() throws Exception {
    setUpVREManager(OTHER_VRE_ID, true);
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(userConfigurationHandler.findVREAuthorization(example)).thenReturn(authorization);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", OTHER_VRE_ID) //
        .put(ClientResponse.class, authorization);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verify(userConfigurationHandler, never()).updateVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteVREAuthorizationAsAdmin() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(userConfigurationHandler.findVREAuthorization(example)).thenReturn(authorization);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NO_CONTENT);

    verify(userConfigurationHandler).deleteVREAuthorization(authorization);
  }

  @Test
  public void testDeleteAuthorizationAsUser() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, USER_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verify(userConfigurationHandler, never()).deleteVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteVREAuthorizationNotLoggedIn() throws Exception {
    setUpVREManager(VRE_ID, true);
    setUserNotLoggedIn();

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verify(userConfigurationHandler, never()).deleteVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteAuthorizationNotFound() throws Exception {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    when(userConfigurationHandler.findVREAuthorization(example)).thenReturn(null);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verify(userConfigurationHandler, never()).deleteVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testDeleteVREAuthorizationNotInScope() throws Exception {
    setUpVREManager(OTHER_VRE_ID, true);
    setupUserWithRoles(OTHER_VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", OTHER_VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verify(userConfigurationHandler, never()).deleteVREAuthorization(any(VREAuthorization.class));
  }

  @Test
  public void testGetRolesAsAdmin() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    ClientResponse response = createResource("roles") //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    List<String> actual = response.getEntity(new GenericType<List<String>>() {});
    assertThat(actual, containsInAnyOrder(UserRoles.getAll().toArray()));
  }

  @Test
  public void testGetRolesAsUser() {
    setUpVREManager(VRE_ID, true);
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = createResource("roles") //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);
  }

  @Test
  public void testGetRolesNotLoggedIn() {
    setUpVREManager(VRE_ID, true);
    setUserNotLoggedIn();

    ClientResponse response = createResource("roles") //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);
  }

  protected User createUser(String id, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }

}
