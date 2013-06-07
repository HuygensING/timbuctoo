package nl.knaw.huygens.repository.server.security.apis;

import java.io.IOException;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.server.security.UserSecurityContext;

import org.surfnet.oaaas.model.VerifyTokenResponse;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

public class SecurityContextCreatorResourceFilter implements ResourceFilter, ContainerRequestFilter {

  private static final String VERIFY_TOKEN_RESPONSE = "VERIFY_TOKEN_RESPONSE";
  private StorageManager storageManager;

  public SecurityContextCreatorResourceFilter(StorageManager storageManager) {
    this.storageManager = storageManager;
  }

  @Override
  public ContainerRequest filter(ContainerRequest request) {
    VerifyTokenResponse verifyTokenResponse = (VerifyTokenResponse) request.getProperties().get(VERIFY_TOKEN_RESPONSE);

    if (verifyTokenResponse != null) {
      User example = new User();
      example.setVreId(verifyTokenResponse.getAudience());
      example.setUserId(verifyTokenResponse.getPrincipal().getName());

      User user = findUser(example);

      if (user == null) {
        user = createUser(example);
      }

      UserSecurityContext securityContext = new UserSecurityContext(verifyTokenResponse.getPrincipal(), user);

      /*
       * TODO: fill setSecure and setAuthenticationScheme
       * ContainerRequest.isSecure wraps SecurityContext.isSecure 
       * the same is true for 
       * ContainerRequest.getAuthenticationScheme()
       */

      request.setSecurityContext(securityContext);
    }

    return request;
  }

  private User createUser(User example) {
    try {
      storageManager.addDocument(User.class, example);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return example;
  }

  private User findUser(final User example) {

    return storageManager.searchDocument(User.class, example);
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