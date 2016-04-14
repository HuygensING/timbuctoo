package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.security.Authorizer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthorizerHelper {
  public static Authorizer userIsNotAllowedToWriteTheCollection(String collectionName, String userId) {
    Authorizer authorizer = mock(Authorizer.class);
    Authorization authorization = mock(Authorization.class);
    given(authorizer.authorizationFor(collectionName, userId)).willReturn(authorization);
    given(authorization.isAllowedToWrite()).willReturn(false);
    return authorizer;
  }

  public static Authorizer anyUserIsAllowedToWriteAnyCollectionAuthorizer() {
    Authorizer allowAllAuthorizer = mock(Authorizer.class);
    Authorization authorization = mock(Authorization.class);
    when(authorization.isAllowedToWrite()).thenReturn(true);
    when(allowAllAuthorizer.authorizationFor(anyString(), anyString())).thenReturn(authorization);
    return allowAllAuthorizer;
  }
}
