package nl.knaw.huygens.repository.rest.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;

import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.rest.filters.UserResourceFilterFactory;
import nl.knaw.huygens.repository.storage.StorageManager;
import nl.knaw.huygens.security.AuthorizationHandler;
import nl.knaw.huygens.security.SecurityInformation;
import nl.knaw.huygens.security.SecurityResourceFilterFactory;
import nl.knaw.huygens.security.UnauthorizedException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/* abstract class names should not end with Test, 
 * because the build script tries to execute all the classes with a Test postfix. 
 */
public abstract class WebServiceTestSetup extends JerseyTest {

  protected static final String USER_ID = "USR000000001";
  protected static final String VRE_ID = "vreID";
  protected static Injector injector;
  private static ResourceTestModule resourceTestModule;

  public WebServiceTestSetup() {
    super(new GuiceTestContainerFactory(injector));
  }

  @BeforeClass
  public static void setUpClass() {
    resourceTestModule = new ResourceTestModule();
    injector = Guice.createInjector(resourceTestModule);
  }

  @Before
  public void setUpAuthorizationHandler() throws UnauthorizedException {
    SecurityInformation securityInformation = new SecurityInformation();
    securityInformation.setApplicationName(VRE_ID);
    securityInformation.setDisplayName(USER_ID);
    securityInformation.setDisplayName("test");
    securityInformation.setPrincipal(mock(Principal.class));

    AuthorizationHandler authorizationHandler = injector.getInstance(AuthorizationHandler.class);
    when(authorizationHandler.getSecurityInformation(any(ContainerRequest.class))).thenReturn(securityInformation);
  }

  @SuppressWarnings("unchecked")
  protected void setUpUserRoles(String userId, ArrayList<String> userRoles) {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    User user = new User();
    user.setId(userId);
    user.setRoles(userRoles);
    user.setVreId(VRE_ID);

    when(storageManager.searchEntity(any(Class.class), any(User.class))).thenReturn(user);
  }

  @After
  public void tearDownMocks() {
    // Reset the mocked object so they influence no tests after the current.
    resourceTestModule.cleanUpMocks();
  }

  @Override
  protected AppDescriptor configure() {
    WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder().build();
    webAppDescriptor.getInitParams().put(PackagesResourceConfig.PROPERTY_PACKAGES,
        "nl.knaw.huygens.repository.rest.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.repository.rest.providers");
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
        SecurityResourceFilterFactory.class.getName() + ";" + RolesAllowedResourceFilterFactory.class.getName() + ";" + UserResourceFilterFactory.class.getName() + ";");
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());

    return webAppDescriptor;
  }

  protected StorageManager getStorageManager() {
    return injector.getInstance(StorageManager.class);
  }

  protected JacksonJsonProvider getJsonProvider() {
    return injector.getInstance(JacksonJsonProvider.class);
  }

  protected WebResource autoResource() {
    return resource().path("resources");
  }

  @SuppressWarnings("unchecked")
  protected void setUserUnauthorized() {
    try {
      AuthorizationHandler authorizationHandler = injector.getInstance(AuthorizationHandler.class);
      when(authorizationHandler.getSecurityInformation(any(ContainerRequest.class))).thenThrow(UnauthorizedException.class);
    } catch (UnauthorizedException ex) {
      //Do nothing
    }
  }

}
