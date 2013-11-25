package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;

import nl.knaw.huygens.security.client.AuthorizationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.filters.SecurityResourceFilterFactory;
import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.rest.filters.UserResourceFilterFactory;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/* abstract class names should not end with Test, 
 * because the build script tries to execute all the classes with a Test postfix. 
 */
public abstract class WebServiceTestSetup extends JerseyTest {

  protected static final String USER_ID = "USER000000001";
  protected static final String VRE_ID = "vreID";
  protected static final String VRE_ID_KEY = "VRE_ID";
  protected static Injector injector;
  private static ResourceTestModule resourceTestModule;

  public WebServiceTestSetup() {
    super(new GuiceTestContainerFactory(injector));
  }

  @BeforeClass
  public static void setupClass() {
    initLogger();
    resourceTestModule = new ResourceTestModule();
    injector = Guice.createInjector(resourceTestModule);
  }

  /**
   * Bridges java.util.logging to SLF4J.
   * See: http://blog.cn-consult.dk/2009/03/bridging-javautillogging-to-slf4j.html
   */
  public static void initLogger() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  @Before
  public void setUpAuthorizationHandler() throws UnauthorizedException {
    HuygensSecurityInformation securityInformation = new HuygensSecurityInformation();
    securityInformation.setDisplayName(USER_ID);
    securityInformation.setDisplayName("test");
    securityInformation.setPrincipal(mock(Principal.class));

    AuthorizationHandler authorizationHandler = injector.getInstance(AuthorizationHandler.class);
    when(authorizationHandler.getSecurityInformation(anyString())).thenReturn(securityInformation);
  }

  protected void setUpUserWithRoles(String userId, ArrayList<String> userRoles) {
    StorageManager storageManager = setUpUser(userId);

    VREAuthorization vreAuthorization = new VREAuthorization();
    vreAuthorization.setRoles(userRoles);
    vreAuthorization.setVreId(VRE_ID);
    vreAuthorization.setUserId(userId);

    VREAuthorization example = new VREAuthorization();
    example.setUserId(userId);
    example.setVreId(VRE_ID);

    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(vreAuthorization);

  }

  protected StorageManager setUpUser(String userId) {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    User user = new User();
    user.setId(userId);

    when(storageManager.findEntity(User.class, user)).thenReturn(user);
    return storageManager;
  }

  @After
  public void tearDownMocks() {
    // Reset the mocked object so they influence no tests after the current.
    resourceTestModule.cleanUpMocks();
  }

  @Override
  protected AppDescriptor configure() {
    WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder().build();
    webAppDescriptor.getInitParams()
        .put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.timbuctoo.rest.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.timbuctoo.rest.providers");
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
        SecurityResourceFilterFactory.class.getName() + ";" + UserResourceFilterFactory.class.getName() + ";" + RolesAllowedResourceFilterFactory.class.getName() + ";");
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

  protected WebResource domainResource(String... pathElements) {
    WebResource resource = resource().path(Paths.DOMAIN_PREFIX);
    for (String pathElement : pathElements) {
      resource = resource.path(pathElement);
    }
    return resource;
  }

  @SuppressWarnings("unchecked")
  protected void setUserNotLoggedIn() {
    try {
      AuthorizationHandler authorizationHandler = injector.getInstance(AuthorizationHandler.class);
      when(authorizationHandler.getSecurityInformation(anyString())).thenThrow(UnauthorizedException.class);
    } catch (UnauthorizedException ex) {
      //Do nothing
    }
  }

}
