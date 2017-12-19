package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.security.dto.Authorization;
import nl.knaw.huygens.timbuctoo.security.dto.UserRoles;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorizationStubs;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationUnavailableException;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.UNVERIFIED_USER_ROLE;
import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.USER_ROLE;
import static nl.knaw.huygens.timbuctoo.security.dto.UserStubs.userWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonBasedAuthorizerTest {

  public static final String VRE_ID = "vreId";
  public static final String USER_ID = "userId";
  private VreAuthorizationAccess authorizationAccess;
  private JsonBasedAuthorizer instance;

  @Before
  public void setUp() throws Exception {
    authorizationAccess = mock(VreAuthorizationAccess.class);
    instance = new JsonBasedAuthorizer(authorizationAccess);
  }

  @Test
  public void authorizationForReturnsTheFoundAuthorizationForTheVreIdAndTheUserId() throws Exception {
    VreAuthorization vreAuthorization = VreAuthorization.create("", "");
    when(authorizationAccess.getAuthorization(anyString(), anyString())).thenReturn(Optional.of(vreAuthorization));

    Authorization authorization = instance.authorizationFor(VRE_ID, USER_ID);

    assertThat(authorization, is(sameInstance(vreAuthorization)));
    verify(authorizationAccess, never()).getOrCreateAuthorization(VRE_ID, USER_ID, UNVERIFIED_USER_ROLE);
  }

  @Test
  public void createAuthorizationLetsCreatesANewAuthorizationForTheUserVreAndRole()
    throws Exception {
    instance.createAuthorization(VRE_ID, userWithId(USER_ID), UserRoles.USER_ROLE);

    verify(authorizationAccess).getOrCreateAuthorization(VRE_ID, USER_ID, UserRoles.USER_ROLE);
  }

  @Test(expected = AuthorizationCreationException.class)
  public void createAuthorizationThrowsAnAuthCreateExWhenTheAuthorizationCollectionThrowsAnAuthUnavailableEx()
    throws Exception {
    when(authorizationAccess.getOrCreateAuthorization(anyString(), anyString(), anyString()))
      .thenThrow(new AuthorizationUnavailableException());

    instance.createAuthorization(VRE_ID, userWithId(USER_ID), UserRoles.USER_ROLE);
  }

  @Test
  public void deleteVreAuthorizationsRemovesAllTheAuthorizationsOfTheVre() throws Exception {
    when(authorizationAccess.getAuthorization(VRE_ID, USER_ID)).thenReturn(Optional.of(
      VreAuthorizationStubs.authorizationWithRole(ADMIN_ROLE)));

    instance.deleteVreAuthorizations(VRE_ID);

    verify(authorizationAccess).deleteVreAuthorizations(VRE_ID);
  }
  
}
