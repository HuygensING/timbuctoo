package nl.knaw.huygens.repository.server.security;

import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.SecurityContext;

import com.google.inject.Inject;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

public class AnnotatedSecurityFilterFactory implements ResourceFilterFactory {
  private OAuthAuthorizationServerConnector authorizationServerConnector;

  @Inject
  public AnnotatedSecurityFilterFactory(OAuthAuthorizationServerConnector authorizationServerConnector) {
    this.authorizationServerConnector = authorizationServerConnector;
  }

  @Override
  public List<ResourceFilter> create(AbstractMethod am) {
    if (am.getAnnotation(RolesAllowed.class) != null || am.getResource().getAnnotation(RolesAllowed.class) != null) {
      return Collections.<ResourceFilter> singletonList(new Filter(authorizationServerConnector));
    }
    return null;
  }

  private class Filter implements ResourceFilter, ContainerRequestFilter {
    private OAuthAuthorizationServerConnector authorizationServerConnector;

    public Filter(OAuthAuthorizationServerConnector authorizationServerConnector) {
      this.authorizationServerConnector = authorizationServerConnector;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
      String authorizationKey = request.getHeaderValue("Authorization");

      SecurityContext securityContext = authorizationServerConnector.authenticate(authorizationKey);
      request.setSecurityContext(securityContext);

      return request;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
      return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
      // TODO Auto-generated method stub
      return null;
    }

  }

}
