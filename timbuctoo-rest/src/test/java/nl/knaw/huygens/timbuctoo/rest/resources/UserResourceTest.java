package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class UserResourceTest extends WebServiceTestSetup {

  private static final String USERS_RESOURCE = "/" + Paths.SYSTEM_PREFIX + "/users";
  private static final String OTHER_USER_ID = "otherUserId";
  private static final String USER_ROLE = "USER";
  private static final String ADMIN_ROLE = "ADMIN";

  private WebResource resource;

  @Before
  public void setupWebResource() {
    resource = resource().path(USERS_RESOURCE);
  }

  @Test
  public void testGetAllUsers() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));

    List<User> expectedList = Lists.newArrayList(createUser("test", "test"), createUser("test1", "test1"), createUser("test", "test"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getAllLimited(User.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<User>> genericType = new GenericType<List<User>>() {};
    List<User> actualList = resource.header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllUsersNonFound() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));

    List<User> expectedList = Lists.newArrayList();
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getAllLimited(User.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<User>> genericType = new GenericType<List<User>>() {};
    List<User> actualList = resource.header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllUsersNotInRole() {
    setUpUserWithRoles(USER_ID, null);

    ClientResponse response = resource.header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetAllUsersNotLoggedIn() {
    setUserNotLoggedIn();

    ClientResponse response = resource.get(ClientResponse.class);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testGetUserAsAdmin() {
    setUpUserWithRoles(OTHER_USER_ID, Lists.newArrayList(ADMIN_ROLE));

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    User actual = resource.path(USER_ID).header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(User.class);
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetUserNotFound() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(null);

    ClientResponse response = resource.path(USER_ID).header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetMyUserDataAsAdmin() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    User actual = resource.path("me").header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(User.class);
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetMyUserDataAsUser() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(USER_ROLE));

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    User actual = resource.path("me").header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(User.class);
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());
  }

  @Test
  public void testGetMyUserDataAsNewUser() throws IOException {
    setUpUser(USER_ID);

    MailSender mailSender = injector.getInstance(MailSender.class);

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    final User admin = createUser("admin", "admin");
    admin.setEmail("admin@admin.com");
    String adminId = "USER00000000002";
    admin.setId(adminId);

    VREAuthorization adminExample = new VREAuthorization();
    adminExample.setVreId(VRE_ID);
    adminExample.setRoles(Lists.newArrayList(ADMIN_ROLE));

    VREAuthorization adminAuth = new VREAuthorization();
    adminAuth.setVreId(VRE_ID);
    adminAuth.setUserId(adminId);
    adminAuth.setRoles(Lists.newArrayList(ADMIN_ROLE));

    when(storageManager.findEntity(VREAuthorization.class, adminExample)).thenReturn(adminAuth);

    when(storageManager.getEntity(User.class, adminId)).thenReturn(admin);

    User actual = resource.path("me").header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getFirstName(), actual.getFirstName());
    assertEquals(expected.getLastName(), actual.getLastName());

    verify(mailSender).sendMail(anyString(), anyString(), anyString());
  }

  @Test
  public void testGetUserAsUser() {
    setUpUserWithRoles(OTHER_USER_ID, Lists.newArrayList(USER_ROLE));

    ClientResponse response = resource.path(USER_ID).header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testGetUserNotLoggedIn() {
    setUserNotLoggedIn();

    ClientResponse response = resource.path(USER_ID).get(ClientResponse.class);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testPutUser() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));
    MailSender sender = injector.getInstance(MailSender.class);

    User user = createUser(USER_ID, "firstName", "lastName");
    user.setEmail("test@test.com");

    User original = createUser(USER_ID, "test", "test");
    original.setEmail("test@test.com");

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(original);

    ClientResponse response = resource.path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, user);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
    verify(sender).sendMail(anyString(), anyString(), anyString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutUserUserNotFound() throws IOException {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));

    User user = createUser(USER_ID, "firstName", "lastName");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws IOException {
        // only if the document version does not exist an IOException is thrown.
        throw new IOException();
      }
    }).when(storageManager).modifyEntity(any(Class.class), any(User.class));

    ClientResponse response = resource.path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, user);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotInRole() {
    setUpUserWithRoles(USER_ID, null);

    User user = createUser(USER_ID, "firstName", "lastName");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(null);

    ClientResponse response = resource.path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef").put(ClientResponse.class, user);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotLoggedIn() {
    User user = createUser(USER_ID, "firstName", "lastName");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(null);

    setUserNotLoggedIn();

    ClientResponse response = resource.path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, user);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUser() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    ClientResponse response = resource.path(USER_ID).header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NO_CONTENT, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserUserNotFound() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(null);

    ClientResponse response = resource.path(USER_ID).header("Authorization", "bearer 12333322abef").header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotInRole() {
    setUpUserWithRoles(USER_ID, null);

    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);

    ClientResponse response = resource.path(USER_ID).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.FORBIDDEN, response.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotLoggedIn() {
    User expected = createUser(USER_ID, "test", "test");
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getEntity(User.class, USER_ID)).thenReturn(expected);
    setUserNotLoggedIn();

    ClientResponse response = resource.path(USER_ID).delete(ClientResponse.class);
    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
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
