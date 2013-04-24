package nl.knaw.huygens.repository.resources;

import static org.mockito.Mockito.mock;

import javax.validation.Validation;
import javax.validation.Validator;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.providers.DocumentReader;
import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.guice.JerseyServletModule;

/**
 * This class mocks the ServletModel used in the webapplication.
 * @author martijnm
 *
 */

class RESTAutoResourceTestModule extends JerseyServletModule {
  private StorageManager storageManager;
  private DocumentTypeRegister documentTypeRegister;
  private OAuthAuthorizationServerConnector oAuthAuthorizationServerConnector;
  private DocumentReader documentReader;
  private JacksonJsonProvider jsonProvider;

  public RESTAutoResourceTestModule() {
    storageManager = mock(StorageManager.class);
    documentTypeRegister = mock(DocumentTypeRegister.class);
    oAuthAuthorizationServerConnector = mock(OAuthAuthorizationServerConnector.class);
    jsonProvider = mock(JacksonJsonProvider.class);
    documentReader = new DocumentReader();
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
  public DocumentTypeRegister providesDocumentTypeRegister() {
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
