package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonBasedAuthorizerTest {
  @Test
  public void authorizationForReturnsTheNewlyCreatedAuthorizationIfTheUserHasNoAuthorizationForTheCurrentCollection()
    throws Exception {
    VreAuthorizationCollection authorizationCollection = mock(VreAuthorizationCollection.class);
    VreAuthorization vreAuthorization = new VreAuthorization();
    when(authorizationCollection.authorizationFor(anyString(), anyString())).thenReturn(Optional.empty());
    when(authorizationCollection.addAuthorizationFor(anyString(), anyString())).thenReturn(vreAuthorization);
    JsonBasedAuthorizer instance = new JsonBasedAuthorizer(authorizationCollection);

    String vreId = "vreId";
    Collection collection = collectionOfVreWithId(vreId);
    String userId = "userId";

    Authorization authorization = instance.authorizationFor(collection, userId);

    assertThat(authorization, is(sameInstance(vreAuthorization)));
    verify(authorizationCollection).addAuthorizationFor(vreId, userId);
  }

  private Collection collectionOfVreWithId(String vreId) {
    Vre vre = mock(Vre.class);
    when(vre.getVreName()).thenReturn(vreId);
    Collection collection = mock(Collection.class);
    when(collection.getVre()).thenReturn(vre);
    return collection;
  }

  @Test
  public void authorizationForReturnsTheFoundAuthorization() throws Exception {
    VreAuthorizationCollection authorizationCollection = mock(VreAuthorizationCollection.class);
    VreAuthorization vreAuthorization = new VreAuthorization();
    when(authorizationCollection.authorizationFor(anyString(), anyString())).thenReturn(Optional.of(vreAuthorization));
    JsonBasedAuthorizer instance = new JsonBasedAuthorizer(authorizationCollection);

    String vreId = "vreId";
    Collection collection = collectionOfVreWithId(vreId);
    String userId = "userId";

    Authorization authorization = instance.authorizationFor(collection, userId);

    assertThat(authorization, is(sameInstance(vreAuthorization)));
    verify(authorizationCollection, never()).addAuthorizationFor(vreId, userId);
  }

  @Test(expected = AuthorizationUnavailableException.class)
  public void authorizationForThrowsAnAuthorizationUnavailableExceptionWhenTheVreAuthorizationsThrowsOneWhileReading()
    throws Exception {
    VreAuthorizationCollection authorizationCollection = mock(VreAuthorizationCollection.class);
    when(authorizationCollection.authorizationFor(anyString(), anyString()))
      .thenThrow(new AuthorizationUnavailableException());
    JsonBasedAuthorizer instance = new JsonBasedAuthorizer(authorizationCollection);

    String vreId = "vreId";
    Collection collection = collectionOfVreWithId(vreId);
    String userId = "userId";

    Authorization authorization = instance.authorizationFor(collection, userId);
  }

  @Test(expected = AuthorizationUnavailableException.class)
  public void authorizationForThrowsAnAuthorizationUnavailableExceptionWhenTheVreAuthorizationsThrowsOneWhileAdding()
    throws Exception {
    VreAuthorizationCollection authorizationCollection = mock(VreAuthorizationCollection.class);
    when(authorizationCollection.authorizationFor(anyString(), anyString()))
      .thenReturn(Optional.empty());
    when(authorizationCollection.addAuthorizationFor(anyString(), anyString()))
      .thenThrow(new AuthorizationUnavailableException());
    JsonBasedAuthorizer instance = new JsonBasedAuthorizer(authorizationCollection);

    String vreId = "vreId";
    Collection collection = collectionOfVreWithId(vreId);
    String userId = "userId";

    Authorization authorization = instance.authorizationFor(collection, userId);
  }

  // TODO: test voor niet uit te lezen bestand
}
