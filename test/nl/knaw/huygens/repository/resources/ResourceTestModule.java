package nl.knaw.huygens.repository.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import javax.validation.Validator;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.mail.MailSender;
import nl.knaw.huygens.repository.managers.SearchManager;
import nl.knaw.huygens.repository.managers.StorageManager;

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
  private Validator validator;
  private MailSender mailSender;
  private SearchManager searchManager;
  private LocalSolrServer localSolrServer;

  public ResourceTestModule() {
    storageManager = mock(StorageManager.class);
    documentTypeRegister = mock(DocTypeRegistry.class);
    jsonProvider = mock(JacksonJsonProvider.class);
    mockApisAuthorizationServerResourceFilter = new MockApisAuthorizationServerResourceFilter();
    validator = mock(Validator.class);
    mailSender = mock(MailSender.class);
    searchManager = mock(SearchManager.class);
    localSolrServer = mock(LocalSolrServer.class);
  }

  /* Because the RestAutoResourceModule is used in a static way for multiple tests,
   * there should be a way to make sure to the mocks are reset to their default behaviour.
   * This method provides this functionality.
   */
  public void cleanUpMocks() {
    reset(storageManager, documentTypeRegister, jsonProvider, validator, mailSender, searchManager, localSolrServer);
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
  public MailSender providesMailSender() {
    return this.mailSender;
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

  @Provides
  public SearchManager provideSearchManager() {
    return searchManager;
  }

  @Provides
  public LocalSolrServer provideLocalSolrServer() {

    return localSolrServer;
  }

}
