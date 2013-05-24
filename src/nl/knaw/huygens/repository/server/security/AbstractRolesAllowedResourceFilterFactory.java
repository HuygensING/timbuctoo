package nl.knaw.huygens.repository.server.security;

import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * Base class for multiple RolesAllowedFilterFactories like ApisAuthorizationResourceFilterFactory and SecurityContextFilterFactory.
 * @author martijnm
 *
 */
public abstract class AbstractRolesAllowedResourceFilterFactory implements ResourceFilterFactory {

  @Inject
  @Named("security.enabled")
  private boolean securityEnabled;

  public AbstractRolesAllowedResourceFilterFactory() {
    super();
  }

  protected abstract ResourceFilter createResourceFilter();

  /**
   * A method needed to override security.
   * @return
   */
  protected abstract ResourceFilter createNoSecurityResourceFilter();

  @Override
  public final List<ResourceFilter> create(AbstractMethod am) {
    if (!hasRolesAllowedAnnotation(am)) {
      return null;
    }
    if (!securityEnabled) {
      return Collections.<ResourceFilter> singletonList(createNoSecurityResourceFilter());
    }

    return Collections.<ResourceFilter> singletonList(createResourceFilter());

  }

  private boolean hasRolesAllowedAnnotation(AbstractMethod am) {
    return am.getAnnotation(RolesAllowed.class) != null || am.getResource().getAnnotation(RolesAllowed.class) != null;
  }

}