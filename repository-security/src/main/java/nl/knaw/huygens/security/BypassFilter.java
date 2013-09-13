package nl.knaw.huygens.security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * A resource filter that can be used to bypass the security for example.
 * @author martijnm
 *
 */
public final class BypassFilter implements ResourceFilter, ContainerRequestFilter {

  @Override
  public ContainerRequest filter(ContainerRequest request) {
    request.setSecurityContext(new SecurityContext() {

      @Override
      public boolean isUserInRole(String role) {
        return true;
      }

      @Override
      public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public Principal getUserPrincipal() {
        return null;
      }

      @Override
      public String getAuthenticationScheme() {
        return null;
      }
    });

    return request;
  }

  @Override
  public ContainerRequestFilter getRequestFilter() {
    return this;
  }

  @Override
  public ContainerResponseFilter getResponseFilter() {
    return null;
  }

}