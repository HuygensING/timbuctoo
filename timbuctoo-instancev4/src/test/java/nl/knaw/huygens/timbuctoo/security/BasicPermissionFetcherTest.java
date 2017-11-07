package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class BasicPermissionFetcherTest {

  private VreAuthorizationCrud vreAuthorizationCrud;
  private PermissionFetcher permissionFetcher;
  private UserValidator userValidator;
  private User testUser;

  @Before
  public void setUp() throws Exception {
    vreAuthorizationCrud = mock(VreAuthorizationCrud.class);
    userValidator = mock(UserValidator.class);
    testUser = mock(User.class);
    given(testUser.getId()).willReturn("testownerid");
    given(userValidator.getUserFromId("testownerid")).willReturn(Optional.of(testUser));
    permissionFetcher = new BasicPermissionFetcher(vreAuthorizationCrud, userValidator);
  }

  @Test
  public void getPermissionsReturnsPermissionsForGivenUserAndDataSet() throws Exception {
    VreAuthorization vreAuthorization = mock(VreAuthorization.class);
    given(vreAuthorization.isAllowedToWrite()).willReturn(true);
    given(vreAuthorizationCrud.getAuthorization(anyString(), anyString())).willReturn(Optional.of(vreAuthorization));

    Set<Permission> permissions = permissionFetcher.getPermissions("testPersistentId",
      "testownerid", "testdatasetid");

    assertThat(permissions, containsInAnyOrder(Permission.WRITE, Permission.READ));
  }

  @Test
  public void getPermissionsReturnsAdminAndReadPermissionsForAdminUserAndDataSet() throws Exception {
    VreAuthorization vreAuthorization = mock(VreAuthorization.class);
    given(vreAuthorization.hasAdminAccess()).willReturn(true);
    given(vreAuthorizationCrud.getAuthorization(anyString(), anyString())).willReturn(Optional.of(vreAuthorization));

    Set<Permission> permissions = permissionFetcher.getPermissions("testPersistentId",
      "testownerid", "testdatasetid");

    assertThat(permissions, containsInAnyOrder(Permission.ADMIN, Permission.READ));
  }

  @Test
  public void getPermissionsReturnsReadPermissionOnlyUserWithoutWritePermissionInDataSet() throws Exception {
    VreAuthorization vreAuthorization = mock(VreAuthorization.class);
    given(vreAuthorization.isAllowedToWrite()).willReturn(false);
    given(vreAuthorizationCrud.getAuthorization(anyString(), anyString())).willReturn(Optional.of(vreAuthorization));

    Set<Permission> permissions = permissionFetcher.getPermissions("testPersistentId",
      "testownerid", "testdatasetid");

    assertThat(permissions, contains(Permission.READ));
  }

  @Test
  public void getPermissionsReturnsReadPermissionWhenAuthorizationUnavailableExceptionTriggered() throws Exception {
    given(vreAuthorizationCrud.getAuthorization(anyString(), anyString())).willThrow(
      AuthorizationUnavailableException.class
    );

    Set<Permission> permissions = permissionFetcher.getPermissions("testPersistentId",
      "testownerid", "testdatasetid");

    assertThat(permissions, contains(Permission.READ));
  }

  @Test
  public void getPermissionsReturnsReadPermissionWhenUserNotPresent() throws Exception {
    given(vreAuthorizationCrud.getAuthorization(anyString(), anyString())).willReturn(Optional.empty());

    Set<Permission> permissions = permissionFetcher.getPermissions("testPersistentId",
      "testownerid", "testdatasetid");

    assertThat(permissions, contains(Permission.READ));
  }

  @Test
  public void initializeOwnerAuthorizationCreatesAdminAuthorization() throws Exception {
    permissionFetcher.initializeOwnerAuthorization("testuserid","testownerid", "testdatasetid");

    verify(vreAuthorizationCrud).createAuthorization("testownerid__testdatasetid", "testuserid", "ADMIN");
  }

  @Test(expected = AuthorizationCreationException.class)
  public void initializeOwnerAuthorizationThrowsExceptionWhenVreAuthorizationCrudFails() throws Exception {
    doThrow(AuthorizationCreationException.class).when(vreAuthorizationCrud)
      .createAuthorization("testownerid__testdatasetid", "testuserid", "ADMIN");

    permissionFetcher.initializeOwnerAuthorization("testuserid","testownerid", "testdatasetid");
  }

  @Test
  public void removeAuthorizationsRemovesAdminAuthorization() throws Exception {
    permissionFetcher.removeAuthorizations("testownerid", "testownerid__testdatasetid");

    verify(vreAuthorizationCrud).deleteVreAuthorizations("testownerid__testdatasetid", testUser);
  }

  @Test(expected = PermissionFetchingException.class)
  public void removeAuthorizationsThrowsExceptionWhenUserRetrievalFails() throws Exception {
    given(testUser.getId()).willReturn("testwrongid");

    permissionFetcher.removeAuthorizations("testwrongid", "testdatasetid");
  }
}
