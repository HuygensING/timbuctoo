package nl.knaw.huygens.repository.server.security.apis;

import nl.knaw.huygens.repository.server.security.AbstractRolesAllowedResourceFilterFactory;
import nl.knaw.huygens.repository.server.security.BypassFilter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.model.AbstractMethod;
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
  protected ResourceFilter createResourceFilter(AbstractMethod am) {
    return new ApisAuthorizationServerResourceFilter(resourceServerKey, resourceServerSecret, authorizationServerUrl, cacheEnabled);
  }

  @Override
  protected ResourceFilter createNoSecurityResourceFilter() {
    return new BypassFilter();
  }
}
