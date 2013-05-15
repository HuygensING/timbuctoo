package nl.knaw.huygens.repository.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import javax.validation.Validation;
import javax.validation.Validator;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;

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
class RESTAutoResourceTestModule extends JerseyServletModule {
  private StorageManager storageManager;
  private DocTypeRegistry documentTypeRegister;
  private OAuthAuthorizationServerConnector oAuthAuthorizationServerConnector;
  private JacksonJsonProvider jsonProvider;

  public RESTAutoResourceTestModule() {
    storageManager = mock(StorageManager.class);
    documentTypeRegister = mock(DocTypeRegistry.class);
    oAuthAuthorizationServerConnector = mock(OAuthAuthorizationServerConnector.class);
    jsonProvider = mock(JacksonJsonProvider.class);
  }

  // Method needed to make sure the mocks affecting only the intended tests.
  public void cleanUpMocks() {
    reset(storageManager);
    reset(documentTypeRegister);
    reset(oAuthAuthorizationServerConnector);
    reset(jsonProvider);
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
  public OAuthAuthorizationServerConnector providesAuthAuthorizationServerConnector() {
    return this.oAuthAuthorizationServerConnector;
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
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

}
