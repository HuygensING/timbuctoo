package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.dto.Authorization;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationUnavailableException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class AuthorizerBuilder {
  public AuthorizerBuilder() {
  }

  public static Authorizer notAllowedToWrite() throws AuthorizationUnavailableException {
    return createAuthorizer(false);
  }

  public static Authorizer allowedToWrite() throws AuthorizationUnavailableException {
    return createAuthorizer(true);
  }

  static Authorizer createAuthorizer(boolean allowedToWrite) throws AuthorizationUnavailableException {
    Authorizer authorizer = Mockito.mock(Authorizer.class);
    Authorization authorization = Mockito.mock(Authorization.class);
    Mockito.when(authorization.isAllowedToWrite()).thenReturn(allowedToWrite);
    Mockito.when(authorizer.authorizationFor(ArgumentMatchers.any(), ArgumentMatchers.anyString()))
           .thenReturn(authorization);
    return authorizer;
  }
}
