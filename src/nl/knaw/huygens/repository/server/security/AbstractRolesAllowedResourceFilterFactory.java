package nl.knaw.huygens.repository.server.security;

import java.lang.annotation.Annotation;
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

  // Used for unit tests.
  AbstractRolesAllowedResourceFilterFactory(boolean securityEnabled) {
    this.securityEnabled = securityEnabled;
  }

  protected abstract ResourceFilter createResourceFilter();

  /**
   * A method needed to override security.
   * @return
   */
  protected abstract ResourceFilter createNoSecurityResourceFilter();

  @Override
  public final List<ResourceFilter> create(AbstractMethod am) {
    if (!hasAnnotation(am, RolesAllowed.class) && !hasAnnotation(am, RolesPartiallyAllowed.class)) {
      return null;
    }
    if (!securityEnabled) {
      return Collections.<ResourceFilter> singletonList(createNoSecurityResourceFilter());
    }

    return Collections.<ResourceFilter> singletonList(createResourceFilter());

  }

  private boolean hasAnnotation(AbstractMethod am, Class<? extends Annotation> annotation) {
    return am.getAnnotation(annotation) != null || am.getResource().getAnnotation(annotation) != null;
  }

}