package nl.knaw.huygens.repository.rest.security;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

public abstract class AbstractSecurityAnnotationResourceFilterFactory implements ResourceFilterFactory {

  @Inject
  @Named("security.enabled")
  protected boolean securityEnabled;

  public AbstractSecurityAnnotationResourceFilterFactory() {
    super();
  }

  protected abstract ResourceFilter createResourceFilter(AbstractMethod am);

  /**
   * A method needed to override security.
   * @return
   */
  protected abstract ResourceFilter createNoSecurityResourceFilter();

  protected boolean hasAnnotation(AbstractMethod am, Class<? extends Annotation> annotation) {
    return am.getAnnotation(annotation) != null || am.getResource().getAnnotation(annotation) != null;
  }

  protected abstract boolean hasRightAnnotations(AbstractMethod am);

  @Override
  public final List<ResourceFilter> create(AbstractMethod am) {
    if (!hasRightAnnotations(am)) {
      return null;
    }
    if (!securityEnabled) {
      return Collections.<ResourceFilter> singletonList(createNoSecurityResourceFilter());
    }

    return Collections.<ResourceFilter> singletonList(createResourceFilter(am));

  }

}