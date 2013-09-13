package nl.knaw.huygens.repository.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.services.mail.MailSender;
import nl.knaw.huygens.repository.storage.StorageManager;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class UserResourceTest extends WebServiceTestSetup {
  private static final String OTHER_USER_ID = "otherUserId";
  private static final String USER_ROLE = "USER";
  private static final String ADMIN_ROLE = "ADMIN";

  @Test
  public void testGetAllUsers() {
    setUpUserRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));
    WebResource webResource = super.resource();

    List<User> expectedList = Lists.<User> newArrayList(createUser("test", "test"), createUser("test1", "test1"), createUser("test", "test"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getAllLimited(User.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<User>> genericType = new GenericType<List<User>>() {};
    List<User> actualList = webResource.path("/resources/user/all").header("Authorization", "bearer 12333322abef").get(genericType);

    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllUsersNonFound() {
    setUpUserRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));
    WebResource webResource = super.resource();

    List<User> expectedList = Lists.<User> newArrayList();
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getAllLimited(User.class, 0, 200)).thenReturn(expectedList);

    GenericType<List<User>> genericType = new GenericType<List<User>>() {};
    List<User> actualList = webResource.path("/resources/user/all").header("Authorization", "bearer 12333322abef").get(genericType);

    assertEquals(expectedList.size(), actualList.size());
  }

  @Test
  public void testGetAllUsersNotInRole() {
    setUpUserRoles(USER_ID, null);
    WebResource webResource = super.resource();

    ClientResponse clientResponse = webResource.path("/resources/user/all").header("Authorization", "bearer 12333322abef").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.FORBIDDEN, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetAllUsersNotLoggedIn() {
    WebResource webResource = super.resource();

    setUserUnauthorized();

    ClientResponse clientResponse = webResource.path("/resources/user/all").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetUserAsAdmin() {
    setUpUserRoles(OTHER_USER_ID, Lists.newArrayList(ADMIN_ROLE));
    WebResource webResource = super.resource();

    User expected = createUser("test", "test");
    expected.setId(USER_ID);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(expected);

    User actual = webResource.path("/resources/user").path(USER_ID).header("Authorization", "bearer 12333322abef").get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.firstName, actual.firstName);
    assertEquals(expected.lastName, actual.lastName);
  }

  @Test
  public void testGetUserNotFound() {
    setUpUserRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));
    WebResource webResource = super.resource();

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(null);

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).header("Authorization", "bearer 12333322abef").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetMyUserDataAsAdmin() {
    setUpUserRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));
    WebResource webResource = super.resource();

    User expected = createUser("test", "test");
    expected.setId(USER_ID);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(expected);

    User actual = webResource.path("/resources/user/me").header("Authorization", "bearer 12333322abef").get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.firstName, actual.firstName);
    assertEquals(expected.lastName, actual.lastName);
  }

  @Test
  public void testGetMyUserDataAsUser() {
    setUpUserRoles(USER_ID, Lists.newArrayList(USER_ROLE));
    WebResource webResource = super.resource();

    User expected = createUser("test", "test");
    expected.setId(USER_ID);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(expected);

    User actual = webResource.path("/resources/user/me").header("Authorization", "bearer 12333322abef").get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.firstName, actual.firstName);
    assertEquals(expected.lastName, actual.lastName);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetMyUserDataAsUnverifiedUser() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList("UNVERIFIED_USER"));
    WebResource webResource = super.resource();

    MailSender mailSender = injector.getInstance(MailSender.class);

    User expected = createUser("test", "test");
    expected.setId(USER_ID);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(expected);

    final User admin = createUser("admin", "admin");
    admin.email = "admin@admin.com";

    final Map<String, User> createdUsers = new HashedMap();

    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        User user = (User) invocation.getArguments()[1];
        user.setId(USER_ID);
        createdUsers.put(USER_ID, user);
        return null;
      }
    }).when(storageManager).addDocument(any(Class.class), any(User.class));

    doAnswer(new Answer<User>() {
      @Override
      public User answer(InvocationOnMock invocation) throws Throwable {

        User user = (User) invocation.getArguments()[1];
        if (user.getRoles() != null && user.getRoles().contains("ADMIN")) {
          return admin;
        }

        return createdUsers.get(USER_ID);
      }
    }).when(storageManager).searchDocument(any(Class.class), any(User.class));

    User actual = webResource.path("/resources/user/me").header("Authorization", "bearer 12333322abef").get(User.class);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.firstName, actual.firstName);
    assertEquals(expected.lastName, actual.lastName);

    verify(mailSender).sendMail(anyString(), anyString(), anyString());
  }

  @Test
  public void testGetUserAsUser() {
    setUpUserRoles(OTHER_USER_ID, Lists.newArrayList(USER_ROLE));
    WebResource webResource = super.resource();

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).header("Authorization", "bearer 12333322abef").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.FORBIDDEN, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetUserNotLoggedIn() {
    WebResource webResource = super.resource();

    setUserUnauthorized();
    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());

  }

  @Test
  public void testPutUser() {
    setUpUserRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));
    MailSender sender = injector.getInstance(MailSender.class);

    User user = createUser("firstName", "lastName");
    user.email = "test@test.com";
    user.setId(USER_ID);

    User original = createUser("test", "test");
    original.email = "test@test.com";
    original.setId(USER_ID);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(original);

    WebResource webResource = super.resource();

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, user);

    assertEquals(ClientResponse.Status.NO_CONTENT, clientResponse.getClientResponseStatus());

    verify(sender).sendMail(anyString(), anyString(), anyString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPutUserUserNotFound() throws IOException {
    setUpUserRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));

    User user = createUser("firstName", "lastName");
    user.setId(USER_ID);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws IOException {
        // only if the document version does not exist an IOException is thrown.
        throw new IOException();
      }
    }).when(storageManager).modifyDocument(any(Class.class), any(User.class));

    WebResource webResource = super.resource();

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, user);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotInRole() {
    setUpUserRoles(USER_ID, null);

    User user = createUser("firstName", "lastName");
    user.setId(USER_ID);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(null);

    WebResource webResource = super.resource();

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer 12333322abef")
        .put(ClientResponse.class, user);

    assertEquals(ClientResponse.Status.FORBIDDEN, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPutUserNotLoggedIn() {
    User user = createUser("firstName", "lastName");
    user.setId(USER_ID);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(null);

    setUserUnauthorized();

    WebResource webResource = super.resource();

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, user);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteUser() {
    setUpUserRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));
    WebResource webResource = super.resource();

    User expected = createUser("test", "test");
    expected.setId(USER_ID);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(expected);

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.OK, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserUserNotFound() {
    setUpUserRoles(USER_ID, Lists.newArrayList(ADMIN_ROLE));
    WebResource webResource = super.resource();

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(null);

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotInRole() {
    setUpUserRoles(USER_ID, null);
    WebResource webResource = super.resource();

    User expected = createUser("test", "test");
    expected.setId(USER_ID);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(expected);

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).header("Authorization", "bearer 12333322abef").delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.FORBIDDEN, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testDeleteUserNotLoggedIn() {
    WebResource webResource = super.resource();

    User expected = createUser("test", "test");
    expected.setId(USER_ID);
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(User.class, USER_ID)).thenReturn(expected);
    setUserUnauthorized();

    ClientResponse clientResponse = webResource.path("/resources/user").path(USER_ID).delete(ClientResponse.class);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, clientResponse.getClientResponseStatus());
  }

  protected User createUser(String firstName, String lastName) {
    User user = new User();
    user.firstName = firstName;
    user.lastName = lastName;

    return user;
  }

}
