package nl.knaw.huygens.repository.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import javax.validation.Validator;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.mail.MailSender;
import nl.knaw.huygens.repository.search.SearchManager;
import nl.knaw.huygens.repository.search.SortableFieldFinder;
import nl.knaw.huygens.repository.storage.StorageManager;

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

  private static final String M0 = "nl.knaw.huygens.repository.model";
  private static final String M1 = "nl.knaw.huygens.repository.variation.model";
  private static final String M1A = "nl.knaw.huygens.repository.variation.model.projecta";
  private static final String M1B = "nl.knaw.huygens.repository.variation.model.projectb";
  private static final String PACKAGES = M0 + " " + M1 + " " + M1A + " " + M1B;

  private DocTypeRegistry docTypeRegistry;
  private StorageManager storageManager;
  private JacksonJsonProvider jsonProvider;
  private MockApisAuthorizationServerResourceFilter mockApisAuthorizationServerResourceFilter;
  private Validator validator;
  private MailSender mailSender;
  private SearchManager searchManager;
  private LocalSolrServer localSolrServer;
  private SortableFieldFinder sortableFieldFinder;

  public ResourceTestModule() {
    docTypeRegistry = new DocTypeRegistry(PACKAGES);
    storageManager = mock(StorageManager.class);
    jsonProvider = mock(JacksonJsonProvider.class);
    mockApisAuthorizationServerResourceFilter = new MockApisAuthorizationServerResourceFilter();
    validator = mock(Validator.class);
    mailSender = mock(MailSender.class);
    searchManager = mock(SearchManager.class);
    localSolrServer = mock(LocalSolrServer.class);
    sortableFieldFinder = mock(SortableFieldFinder.class);

  }

  /* Because the RestAutoResourceModule is used in a static way for multiple tests,
   * there should be a way to make sure to the mocks are reset to their default behaviour.
   * This method provides this functionality.
   */
  public void cleanUpMocks() {
    reset(storageManager, jsonProvider, validator, mailSender, searchManager, localSolrServer);
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
    return this.docTypeRegistry;
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

  @Provides
  public SortableFieldFinder prSortableFieldFinder() {
    return sortableFieldFinder;
  }

}
