package nl.knaw.huygens.repository.server.security;

import javax.annotation.security.RolesAllowed;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * Base class for multiple RolesAllowedFilterFactories like ApisAuthorizationResourceFilterFactory and SecurityContextFilterFactory.
 * @author martijnm
 *
 */
public abstract class AbstractRolesAllowedResourceFilterFactory extends AbstractSecurityAnnotationResourceFilterFactory implements ResourceFilterFactory {

  public AbstractRolesAllowedResourceFilterFactory() {
    super();
  }

  // Used for unit tests.
  AbstractRolesAllowedResourceFilterFactory(boolean securityEnabled) {
    this.securityEnabled = securityEnabled;
  }

  @Override
  protected abstract ResourceFilter createResourceFilter(AbstractMethod am);

  /**
   * A method needed to override security.
   * @return
   */
  @Override
  protected abstract ResourceFilter createNoSecurityResourceFilter();

  @Override
  protected boolean hasRightAnnotations(AbstractMethod am) {
    return hasAnnotation(am, RolesAllowed.class) || hasAnnotation(am, RolesPartiallyAllowed.class);
  }
}