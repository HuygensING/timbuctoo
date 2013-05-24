package nl.knaw.huygens.repository.server.security.apis;

import nl.knaw.huygens.repository.server.security.AbstractRolesAllowedResourceFilterFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

public class ApisAuthorizationResourceFilterFactory extends AbstractRolesAllowedResourceFilterFactory {
  private String resourceServerKey;
  private String resourceServerSecret;
  private String authorizationServerUrl;

  private boolean cacheEnabled;

  @Inject
  public ApisAuthorizationResourceFilterFactory(@Named("security.apis.key") String resourceServerKey, @Named("security.apis.secret") String resourceServerSecret,
      @Named("security.apis.server") String authorizationServerUrl, @Named("security.apis.cache") boolean cacheEnabled) {
    this.resourceServerKey = resourceServerKey;
    this.resourceServerSecret = resourceServerSecret;
    this.authorizationServerUrl = authorizationServerUrl;
    this.cacheEnabled = cacheEnabled;
  }

  @Override
  protected ResourceFilter createResourceFilter() {
    return new ApisAuthorizationServerResourceFilter(resourceServerKey, resourceServerSecret, authorizationServerUrl, cacheEnabled);
  }

  @Override
  protected ResourceFilter createNoSecurityResourceFilter() {
    return new NoSecurityFilter();
  }

  private static final class NoSecurityFilter implements ResourceFilter, ContainerRequestFilter {

    @Override
    public ContainerRequest filter(ContainerRequest request) {
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
}
