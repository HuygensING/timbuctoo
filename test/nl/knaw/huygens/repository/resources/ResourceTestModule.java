package nl.knaw.huygens.repository.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import javax.validation.Validator;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.managers.StorageManager;

import org.surfnet.oaaas.model.VerifyTokenResponse;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.guice.JerseyServletModule;

/**
 * This class mocks the ServletModel used in the webapplication.
 *
 * @author martijnm
 */
class ResourceTestModule extends JerseyServletModule {
  private StorageManager storageManager;
  private DocTypeRegistry documentTypeRegister;
  private JacksonJsonProvider jsonProvider;
  private MockApisAuthorizationServerResourceFilter mockApisAuthorizationServerResourceFilter;
  private VerifyTokenResponse verifyTokenResponse;
  private Validator validator;

  public ResourceTestModule() {
    storageManager = mock(StorageManager.class);
    documentTypeRegister = mock(DocTypeRegistry.class);
    jsonProvider = mock(JacksonJsonProvider.class);
    verifyTokenResponse = mock(VerifyTokenResponse.class);
    mockApisAuthorizationServerResourceFilter = new MockApisAuthorizationServerResourceFilter();
    validator = mock(Validator.class);
  }

  /* Because the RestAutoResourceModule is used in a static way for multiple tests,
   * there should be a way to make sure to the mocks are reset to their default behaviour.
   * This method provides this functionality.
   */
  public void cleanUpMocks() {
    reset(storageManager);
    reset(documentTypeRegister);
    reset(jsonProvider);
    reset(verifyTokenResponse);
    reset(validator);
  }

  @Override
  protected void configureServlets() {
    bind(RESTAutoResource.class);
    super.configureServlets();
  }

  @Provides
  public StorageManager providesStorageManager() {
    return this.storageManager;
  }

  @Provides
  public DocTypeRegistry providesDocumentTypeRegister() {
    return this.documentTypeRegister;
  }

  @Provides
  public MockApisAuthorizationServerResourceFilter provideMockApisAuthorizationServerResourceFilter() {
    return this.mockApisAuthorizationServerResourceFilter;
  }

  @Singleton
  @Provides
  public JacksonJsonProvider providesJsonProvider() {
    return this.jsonProvider;
  }

  @Provides
  @Named(value = "public_url")
  public String providesPublicURL() {
    return "";
  }

  @Provides
  @Named(value = "html.defaultstylesheet")
  public String providesDefaultStyleSheet() {
    return "";
  }

  @Provides
  @Singleton
  Validator provideValidator() {
    return this.validator;
  }

  @Provides
  @Named(value = "security.enabled")
  public boolean provideSecurityEnabled() {
    return true;
  }

}
