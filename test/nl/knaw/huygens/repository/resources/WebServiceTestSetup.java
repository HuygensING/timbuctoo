package nl.knaw.huygens.repository.resources;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;

import org.junit.After;
import org.junit.BeforeClass;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/* abstract class names should not end with Test, 
 * because the build script tries to execute all the classes with a Test postfix. 
 */
public abstract class WebServiceTestSetup extends JerseyTest {

  protected static Injector injector;
  private static RESTAutoResourceTestModule restAutoResourceTestModule;
  protected SecurityContext securityContext;
  protected OAuthAuthorizationServerConnector oAuthAuthorizationServerConnector;

  public WebServiceTestSetup() {
    super(new GuiceTestContainerFactory(injector));
  }

  @BeforeClass
  public static void setUpClass() {
    restAutoResourceTestModule = new RESTAutoResourceTestModule();
    injector = Guice.createInjector(restAutoResourceTestModule);
  }

  @After
  public void tearDownMocks() {
    // Reset the mocked object so they influence no tests after the current.
    restAutoResourceTestModule.cleanUpMocks();
  }

  @Override
  protected AppDescriptor configure() {
    WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder().build();
    webAppDescriptor.getInitParams().put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.repository.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.repository.providers");
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
        "nl.knaw.huygens.repository.server.security.AnnotatedSecurityFilterFactory;com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory");

    return webAppDescriptor;
  }

}