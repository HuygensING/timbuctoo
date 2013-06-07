package nl.knaw.huygens.repository.server.security;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

public class RolesPartiallyAllowedResourceFilter implements ResourceFilter, ContainerRequestFilter {
  private String[] rolesFullyAllowed;
  private String[] ownDataAllowed;

  public RolesPartiallyAllowedResourceFilter(String[] rolesFullyAllowed, String[] ownDataAllowed) {
    this.rolesFullyAllowed = rolesFullyAllowed;
    this.ownDataAllowed = ownDataAllowed;
  }

  @Override
  public ContainerRequest filter(ContainerRequest request) {

    SecurityContext securityContext = request.getSecurityContext();

    if (rolesFullyAllowed != null) {
      for (String fullyAllowed : rolesFullyAllowed) {
        if (securityContext.isUserInRole(fullyAllowed)) {
          return request;
        }
      }
    }

    if (ownDataAllowed != null) {
      for (String partiallyAllowed : ownDataAllowed) {
        if (securityContext.isUserInRole(partiallyAllowed)) {
          if (isUserAllowedToViewItem(securityContext, request.getPathSegments())) {
            return request;
          }
        }
      }
    }

    throw new WebApplicationException(Response.Status.FORBIDDEN);
  }

  private boolean isUserAllowedToViewItem(SecurityContext securityContext, List<PathSegment> pathSegments) {
    boolean idFound = false;
    boolean userInPath = false;
    if (securityContext instanceof UserSecurityContext) {
      UserSecurityContext userSecurityContext = (UserSecurityContext) securityContext;
      for (PathSegment pathSegment : pathSegments) {
        if (pathSegment.getPath().equals(userSecurityContext.getUser().getId())) {
          idFound = true;
        } else if (pathSegment.getPath().equals("user")) {
          userInPath = true;
        }
      }
      return idFound && userInPath;
    }

    return false;
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
