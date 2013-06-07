package nl.knaw.huygens.repository.server.security;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

public class RolesPartiallyAllowedResourceFilterFactory extends AbstractSecurityAnnotationResourceFilterFactory implements ResourceFilterFactory {

  @Override
  protected ResourceFilter createResourceFilter(AbstractMethod am) {
    RolesPartiallyAllowed annotation = am.getAnnotation(RolesPartiallyAllowed.class);
    String[] rolesFullyAllowed = annotation.fullyAllowed();
    String[] ownDataAllowed = annotation.ownDataAllowed();

    return new RolesPartiallyAllowedResourceFilter(rolesFullyAllowed, ownDataAllowed);
  }

  @Override
  protected ResourceFilter createNoSecurityResourceFilter() {
    return new BypassFilter();
  }

  @Override
  protected boolean hasRightAnnotations(AbstractMethod am) {
    return hasAnnotation(am, RolesPartiallyAllowed.class);
  }

}
