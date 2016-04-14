package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
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

    given(
      authorizer.authorizationFor(argThat(likeCollection().withCollectionName(collectionName)), argThat(is(userId))))
      .willReturn(authorization);
    given(authorization.isAllowedToWrite()).willReturn(false);
    return authorizer;
  }

  public static Authorizer anyUserIsAllowedToWriteAnyCollectionAuthorizer() {
    Authorizer allowAllAuthorizer = mock(Authorizer.class);
    Authorization authorization = mock(Authorization.class);
    when(authorization.isAllowedToWrite()).thenReturn(true);
    when(allowAllAuthorizer.authorizationFor(any(Collection.class), anyString())).thenReturn(authorization);
    return allowAllAuthorizer;
  }
}
