package nl.knaw.huygens.security;

import com.sun.jersey.spi.container.ContainerRequest;

/**
 * Get the data from a ContainerRequest, that is required for the SecurityInformation.
 * This interface should be used to communicate with 3rd party security services. 
 */
public interface AuthorizationHandler {

  /**
   * Extracts the information for needed for creating a SecurityContext from a ContainerRequest.
   * The implementation of this interface will be dependent on the 3rd party security implementation.
   * @param request should contain user information.
   * @return the information needed to create a SecurityContext.
   * @throws UnauthorizedException will be thrown when no or non-valid user information is sent with the request.
   */
  SecurityInformation getSecurityInformation(ContainerRequest request) throws UnauthorizedException;
}
