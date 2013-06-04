package nl.knaw.huygens.repository.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.server.security.apis.SecurityContextCreatorResourceFilterFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.surfnet.oaaas.auth.principal.AuthenticatedPrincipal;
import org.surfnet.oaaas.model.VerifyTokenResponse;

import com.google.inject.Guice;
import com.google.inject.Injector;
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

  private static final String USER_ID = "userId";
  private static final String VRE_ID = "vreID";
  protected static Injector injector;
  private static ResourceTestModule restAutoResourceTestModule;

  public WebServiceTestSetup() {
    super(new GuiceTestContainerFactory(injector));
  }

  @BeforeClass
  public static void setUpClass() {
    restAutoResourceTestModule = new ResourceTestModule();
    injector = Guice.createInjector(restAutoResourceTestModule);
  }

  @Before
  public void setUpApisAuthorizationServerFilterMock() {
    VerifyTokenResponse verifyTokenResponse = mock(VerifyTokenResponse.class);
    when(verifyTokenResponse.getAudience()).thenReturn(VRE_ID);
    AuthenticatedPrincipal authenticatedPrincipal = mock(AuthenticatedPrincipal.class);
    when(authenticatedPrincipal.getName()).thenReturn(USER_ID);
    when(verifyTokenResponse.getPrincipal()).thenReturn(authenticatedPrincipal);
    when(verifyTokenResponse.getError()).thenReturn(null);
    MockApisAuthorizationServerResourceFilter filter = injector.getInstance(MockApisAuthorizationServerResourceFilter.class);
    filter.setVerifyTokenResponse(verifyTokenResponse);

  }

  @SuppressWarnings("unchecked")
  protected void setUpUserRoles(ArrayList<String> userRoles) {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(USER_ID);
    when(user.getVreId()).thenReturn(VRE_ID);
    when(user.getRoles()).thenReturn(userRoles);
    when(storageManager.searchDocument(any(Class.class), any(Document.class))).thenReturn(user);
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
        MockApisAuthorizationFilterFactory.class.getName() + ";" + SecurityContextCreatorResourceFilterFactory.class.getName() + ";" + RolesAllowedResourceFilterFactory.class.getName());

    return webAppDescriptor;
  }
}