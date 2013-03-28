package nl.knaw.huygens.repository.resources;

import static org.mockito.Mockito.mock;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;

import com.google.inject.Provides;
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

  public RESTAutoResourceTestModule() {
    storageManager = mock(StorageManager.class);
    documentTypeRegister = mock(DocumentTypeRegister.class);
    oAuthAuthorizationServerConnector = mock(OAuthAuthorizationServerConnector.class);
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

}
