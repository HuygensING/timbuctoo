package nl.knaw.huygens.security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * The SecurityResourceFilter uses an AuthorizationHandler to get the mandatory information to create a SecurityContext.
 * This SecurityContext is created by a SecurityContextCreator.
 * If the SecurityContext is null, it will not be set to the ContainerRequest. 
 *
 */
public final class SecurityResourceFilter implements ResourceFilter, ContainerRequestFilter {
  protected final SecurityContextCreator securityContextCreator;
  protected final AuthorizationHandler authorizationHandler;

  protected SecurityResourceFilter(SecurityContextCreator securityContextCreator, AuthorizationHandler authorizationHandler) {
    this.securityContextCreator = securityContextCreator;
    this.authorizationHandler = authorizationHandler;
  }

  @Override
  public ContainerRequest filter(ContainerRequest request) {

    SecurityContext securityContext = createSecurityContext(request);

    if (securityContext != null) {

      /*
       * TODO: fill setSecure and setAuthenticationScheme
       * ContainerRequest.isSecure wraps SecurityContext.isSecure 
       * the same is true for 
       * ContainerRequest.getAuthenticationScheme()
       */
      request.setSecurityContext(securityContext);
    }

    return request;
  }

  protected SecurityContext createSecurityContext(ContainerRequest request) {
    SecurityInformation securityInformation;
    try {
      securityInformation = authorizationHandler.getSecurityInformation(request);
    } catch (UnauthorizedException e) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }

    return securityContextCreator.createSecurityContext(securityInformation);
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
