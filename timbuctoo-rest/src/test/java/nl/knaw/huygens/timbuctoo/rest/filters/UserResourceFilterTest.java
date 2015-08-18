package nl.knaw.huygens.timbuctoo.rest.filters;

import com.sun.jersey.spi.container.ContainerRequest;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContext;
import nl.knaw.huygens.timbuctoo.security.VREAuthorizationHandler;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.WebApplicationException;

import static org.mockito.Mockito.mock;

public class UserResourceFilterTest {

  @Test(expected = WebApplicationException.class)
  public void filterThrowsAWebApplicationExceptionWithStatusUnauthorizedIfThereIsNoUserInTheSercurityContext() {
    // setup
    UserResourceFilterFactory.UserResourceFilter instance = new UserResourceFilterFactory.UserResourceFilter(mock(VREAuthorizationHandler.class));
    UserSecurityContext securityContext = mock(UserSecurityContext.class);
    ContainerRequest request = mock(ContainerRequest.class);
    Mockito.when(request.getSecurityContext()).thenReturn(securityContext);

    // action
    instance.filter(request);
  }
}
