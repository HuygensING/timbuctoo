package nl.knaw.huygens.repository.server.security;

import javax.ws.rs.core.SecurityContext;

import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class SecurityFilter implements ContainerRequestFilter {
  private OAuthAuthorizationServerConnector oAuthAuthorizationServerConnector;

  @Inject
  public SecurityFilter(OAuthAuthorizationServerConnector oAuthAuthorizationServerConnector) {
    this.oAuthAuthorizationServerConnector = oAuthAuthorizationServerConnector;
  }

  @Override
  public ContainerRequest filter(ContainerRequest request) {

    //Only check the authentication when the method will modify.
    if (!"GET".equals(request.getMethod())) {

      String authorizationKey = request.getHeaderValue("Authorization");

      SecurityContext securityContext = oAuthAuthorizationServerConnector.authenticate(authorizationKey);
      request.setSecurityContext(securityContext);
    }

    return request;
  }
}
