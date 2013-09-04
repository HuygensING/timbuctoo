package nl.knaw.huygens.repository.rest.security;

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