package nl.knaw.huygens.repository.server.security.apis;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.repository.server.security.AbstractRolesAllowedResourceFilterFactory;

import org.surfnet.oaaas.model.VerifyTokenResponse;

import com.google.common.collect.Lists;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * 
 * This factory creates a class, that should extract the data from the VerifyTokenResponse, added by the ApisRolesAllowedAuthenticationFilter.
 * @author martijnm
 */
public class SecurityContextCreatorResourceFilterFactory extends AbstractRolesAllowedResourceFilterFactory {

  @Override
  protected ResourceFilter createResourceFilter() {
    return new Filter();
  }

  private static final class Filter implements ResourceFilter, ContainerRequestFilter {

    private static final String VERIFY_TOKEN_RESPONSE = "VERIFY_TOKEN_RESPONSE";

    @Override
    public ContainerRequest filter(ContainerRequest request) {
      VerifyTokenResponse verifyTokenResponse = (VerifyTokenResponse) request.getProperties().get(VERIFY_TOKEN_RESPONSE);

      if (verifyTokenResponse != null) {
        ApisAuthorizer securityContext = new ApisAuthorizer(verifyTokenResponse.getPrincipal());
        securityContext.setRoles(Lists.newArrayList("USER"));
        request.setSecurityContext(securityContext);
      }

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

  private static final class NoSecuritityFilter implements ResourceFilter, ContainerRequestFilter {

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

  @Override
  protected ResourceFilter createNoSecurityResourceFilter() {
    return new NoSecuritityFilter();
  }

}
