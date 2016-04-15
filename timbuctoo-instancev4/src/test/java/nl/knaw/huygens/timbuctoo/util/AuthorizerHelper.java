package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;

import static nl.knaw.huygens.timbuctoo.util.CollectionMatcher.likeCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthorizerHelper {
  public static Authorizer userIsNotAllowedToWriteTheCollection(String collectionName, String userId) {
    Authorizer authorizer = mock(Authorizer.class);
    Authorization authorization = mock(Authorization.class);

    try {
      given(
        authorizer.authorizationFor(argThat(likeCollection().withCollectionName(collectionName)), argThat(is(userId))))
        .willReturn(authorization);
    } catch (AuthorizationUnavailableException e) {
      e.printStackTrace();
    }
    given(authorization.isAllowedToWrite()).willReturn(false);
    return authorizer;
  }

  public static Authorizer anyUserIsAllowedToWriteAnyCollectionAuthorizer() {
    Authorizer allowAllAuthorizer = mock(Authorizer.class);
    Authorization authorization = mock(Authorization.class);
    when(authorization.isAllowedToWrite()).thenReturn(true);
    try {
      when(allowAllAuthorizer.authorizationFor(any(Collection.class), anyString())).thenReturn(authorization);
    } catch (AuthorizationUnavailableException e) {
      e.printStackTrace();
    }
    return allowAllAuthorizer;
  }

  public static Authorizer authorizerThrowsAuthorizationUnavailableException()
    throws AuthorizationUnavailableException {
    Authorizer authorizer = mock(Authorizer.class);
    when(authorizer.authorizationFor(any(Collection.class), anyString()))
      .thenThrow(new AuthorizationUnavailableException());
    return authorizer;
  }
}
