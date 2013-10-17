package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import javax.validation.Validator;

import nl.knaw.huygens.security.AuthorizationHandler;
import nl.knaw.huygens.security.SecurityContextCreator;
import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.index.LocalSolrServer;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContextCreator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.guice.JerseyServletModule;

/**
 * This class mocks the ServletModel used in the webapplication,
 * except for DocTypeRegistry which is the real thing.
 */
class ResourceTestModule extends JerseyServletModule {

  private static final String M0 = "nl.knaw.huygens.timbuctoo.model";
  private static final String M1 = "nl.knaw.huygens.timbuctoo.rest.providers.model";
  private static final String M1A = "nl.knaw.huygens.timbuctoo.rest.providers.model.projecta";
  private static final String M1B = "nl.knaw.huygens.timbuctoo.rest.providers.model.projectb";
  private static final String PACKAGES = M0 + " " + M1 + " " + M1A + " " + M1B;

  private DocTypeRegistry typeRegistry;
  private StorageManager storageManager;
  private JacksonJsonProvider jsonProvider;
  private Validator validator;
  private MailSender mailSender;
  private SearchManager searchManager;
  private LocalSolrServer localSolrServer;
  private SecurityContextCreator securityContextCreator;
  private AuthorizationHandler authorizationHandler;

  public ResourceTestModule() {
    typeRegistry = new DocTypeRegistry(PACKAGES);
    storageManager = mock(StorageManager.class);
    jsonProvider = mock(JacksonJsonProvider.class);
    validator = mock(Validator.class);
    mailSender = mock(MailSender.class);
    searchManager = mock(SearchManager.class);
    localSolrServer = mock(LocalSolrServer.class);
    securityContextCreator = new UserSecurityContextCreator(storageManager);
    authorizationHandler = mock(AuthorizationHandler.class);

  }

  /* Because the RestAutoResourceModule is used in a static way for multiple tests,
   * there should be a way to make sure to the mocks are reset to their default behaviour.
   * This method provides this functionality.
   */
  public void cleanUpMocks() {
    reset(storageManager, jsonProvider, validator, mailSender, searchManager, localSolrServer, authorizationHandler);
  }

  @Override
  protected void configureServlets() {
    bind(DomainEntityResource.class);
    super.configureServlets();
  }

  @Provides
  public StorageManager providesStorageManager() {
    return this.storageManager;
  }

  @Provides
  public DocTypeRegistry providesDocumentTypeRegister() {
    return this.typeRegistry;
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
  @Singleton
  public SearchManager provideSearchManager() {
    return searchManager;
  }

  @Provides
  public LocalSolrServer provideLocalSolrServer() {
    return localSolrServer;
  }

  @Provides
  @Singleton
  public SecurityContextCreator provideSecurityContextCreator() {
    return securityContextCreator;
  }

  @Provides
  @Singleton
  public AuthorizationHandler provideAuthorizationHandler() {
    return authorizationHandler;
  }
}
