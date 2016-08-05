package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonBasedAuthorizerTest {

  public static final String VRE_ID = "vreId";
  public static final String USER_ID = "userId";
  private VreAuthorizationCollection authorizationCollection;
  private JsonBasedAuthorizer instance;

  @Before
  public void setUp() throws Exception {
    authorizationCollection = mock(VreAuthorizationCollection.class);
    instance = new JsonBasedAuthorizer(authorizationCollection);
  }

  @Test
  public void authorizationForReturnsTheNewAuthorizationIfTheUserHasNoAuthorizationForTheCurrentCollection()
    throws Exception {
    VreAuthorization vreAuthorization = new VreAuthorization();
    when(authorizationCollection.authorizationFor(anyString(), anyString())).thenReturn(Optional.empty());
    when(authorizationCollection.addAuthorizationFor(anyString(), anyString(), anyString()))
      .thenReturn(vreAuthorization);

    Collection collection = collectionOfVreWithId(VRE_ID);
    String userId = USER_ID;

    Authorization authorization = instance.authorizationFor(collection, userId);

    assertThat(authorization, is(sameInstance(vreAuthorization)));
    verify(authorizationCollection).addAuthorizationFor(VRE_ID, userId, UNVERIFIED_USER_ROLE);
  }

  private Collection collectionOfVreWithId(String vreId) {
    Vre vre = mock(Vre.class);
    when(vre.getVreName()).thenReturn(vreId);
    Collection collection = mock(Collection.class);
    when(collection.getVre()).thenReturn(vre);
    return collection;
  }

  @Test
  public void authorizationForReturnsTheFoundAuthorizationForTheVreOfTheCollection() throws Exception {
    VreAuthorization vreAuthorization = new VreAuthorization();
    when(authorizationCollection.authorizationFor(anyString(), anyString())).thenReturn(Optional.of(vreAuthorization));
    Collection collection = collectionOfVreWithId(VRE_ID);

    Authorization authorization = instance.authorizationFor(collection, USER_ID);

    assertThat(authorization, is(sameInstance(vreAuthorization)));
    verify(authorizationCollection, never()).addAuthorizationFor(VRE_ID, USER_ID, UNVERIFIED_USER_ROLE);
  }

  @Test
  public void authorizationForReturnsTheFoundAuthorizationForTheVreIdAndTheUserId() throws Exception {
    VreAuthorization vreAuthorization = new VreAuthorization();
    when(authorizationCollection.authorizationFor(anyString(), anyString())).thenReturn(Optional.of(vreAuthorization));

    Authorization authorization = instance.authorizationFor(VRE_ID, USER_ID);

    assertThat(authorization, is(sameInstance(vreAuthorization)));
    verify(authorizationCollection, never()).addAuthorizationFor(VRE_ID, USER_ID, UNVERIFIED_USER_ROLE);
  }

  @Test(expected = AuthorizationUnavailableException.class)
  public void authorizationForThrowsAnAuthorizationUnavailableExceptionWhenTheVreAuthorizationsThrowsOneWhileReading()
    throws Exception {
    when(authorizationCollection.authorizationFor(anyString(), anyString()))
      .thenThrow(new AuthorizationUnavailableException());
    Collection collection = collectionOfVreWithId(VRE_ID);

    instance.authorizationFor(collection, USER_ID);
  }

  @Test(expected = AuthorizationUnavailableException.class)
  public void authorizationForThrowsAnAuthorizationUnavailableExceptionWhenTheVreAuthorizationsThrowsOneWhileAdding()
    throws Exception {
    when(authorizationCollection.authorizationFor(anyString(), anyString()))
      .thenReturn(Optional.empty());
    when(authorizationCollection.addAuthorizationFor(anyString(), anyString(), anyString()))
      .thenThrow(new AuthorizationUnavailableException());
    Collection collection = collectionOfVreWithId(VRE_ID);

    instance.authorizationFor(collection, USER_ID);
  }

  @Test
  public void createAuthorizationLetsCreatesANewAuthorizationForTheUserVreAndRole()
    throws Exception {
    instance.createAuthorization(VRE_ID, USER_ID, UserRoles.USER_ROLE);

    verify(authorizationCollection).addAuthorizationFor(VRE_ID, USER_ID, UserRoles.USER_ROLE);
  }

  @Test(expected = AuthorizationCreationException.class)
  public void createAuthorizationThrowsAnAuthCreateExWhenTheAuthorizationCollectionThrowsAnAuthUnavailableEx()
    throws Exception {
    when(authorizationCollection.addAuthorizationFor(anyString(), anyString(), anyString()))
      .thenThrow(new AuthorizationUnavailableException());

    instance.createAuthorization(VRE_ID, USER_ID, UserRoles.USER_ROLE);
  }

}
