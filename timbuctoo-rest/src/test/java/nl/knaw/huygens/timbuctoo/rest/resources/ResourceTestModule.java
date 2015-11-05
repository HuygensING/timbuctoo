package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.guice.JerseyServletModule;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.SecurityContextCreator;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequests;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.rest.util.AdditionalFilterTranslator;
import nl.knaw.huygens.timbuctoo.rest.util.AutocompleteResultConverter;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.RegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.RelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchRequestValidator;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.search.converters.SearchParametersConverter;
import nl.knaw.huygens.timbuctoo.security.BasicAuthenticationHandler;
import nl.knaw.huygens.timbuctoo.security.DefaultVREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContextCreator;
import nl.knaw.huygens.timbuctoo.security.VREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import javax.validation.Validator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

/**
 * This class mocks the ServletModel used in the webapplication,
 * except for DocTypeRegistry which is the real thing.
 */
class ResourceTestModule extends JerseyServletModule {

  private static final String M0 = "timbuctoo.model";
  private static final String M1 = "test.rest.model";
  private static final String M1A = "test.rest.model.projecta";
  private static final String M1B = "test.rest.model.projectb";
  private static final String PACKAGES = M0 + " " + M1 + " " + M1A + " " + M1B;
  public static final String PERSISTENCE_PRODUCER = "persistenceProducer";

  //All the classes are instance variables because we need to be able to reset them after each test.
  private Configuration config;
  private TypeRegistry typeRegistry;
  private Repository repository;
  private JacksonJsonProvider jsonProvider;
  private Validator validator;
  private MailSender mailSender;
  private SecurityContextCreator securityContextCreator;
  private AuthenticationHandler authenticationHandler;
  private Broker broker;
  private Producer indexProducer;
  private Producer persistenceProducer;
  private IndexManager indexManager;
  private SearchRequestValidator searchRequestValidator;
  private SearchParametersConverter searchParametersConverter;
  private RelationSearcher relationSearcher;
  private RegularSearchResultMapper regularClientSearchResultCreator;
  private IndexRegularSearchResultMapper indexRegularSearchResultMapper;
  private IndexRelationSearchResultMapper indexRelationSearchResultMapper;
  private RelationSearchResultMapper relationClientSearchResultCreator;
  private UserConfigurationHandler userConfigurationHandler;
  private BasicAuthenticationHandler basicAuthenticationHandler;
  private ChangeHelper changeHelper;
  private VRECollection vreCollection;
  private AutocompleteResultConverter autoCompleteResultConverter;
  private AdditionalFilterTranslator additionalfilterTranslator;
  private IndexRequests indexRequests;
  private IndexRequestFactory indexRequestFactory;
  private RelationSearchParametersConverter relationSearchParametersConverter;


  public ResourceTestModule() {
    try {
      config = mock(Configuration.class);
      typeRegistry = TypeRegistry.getInstance().init(PACKAGES);
      repository = mock(Repository.class);

      jsonProvider = mock(JacksonJsonProvider.class);
      validator = mock(Validator.class);
      mailSender = mock(MailSender.class);
      userConfigurationHandler = mock(UserConfigurationHandler.class);
      securityContextCreator = new UserSecurityContextCreator(userConfigurationHandler);
      authenticationHandler = mock(AuthenticationHandler.class);
      broker = mock(Broker.class);
      indexProducer = mock(Producer.class);
      persistenceProducer = mock(Producer.class);
      indexManager = mock(IndexManager.class);
      searchRequestValidator = mock(SearchRequestValidator.class);
      searchParametersConverter = mock(SearchParametersConverter.class);
      relationSearcher = mock(RelationSearcher.class);
      regularClientSearchResultCreator = mock(RegularSearchResultMapper.class);
      relationClientSearchResultCreator = mock(RelationSearchResultMapper.class);
      basicAuthenticationHandler = mock(BasicAuthenticationHandler.class);
      changeHelper = mock(ChangeHelper.class);
      vreCollection = mock(VRECollection.class);
      autoCompleteResultConverter = mock(AutocompleteResultConverter.class);
      additionalfilterTranslator = mock(AdditionalFilterTranslator.class);
      indexRegularSearchResultMapper = mock(IndexRegularSearchResultMapper.class);
      indexRelationSearchResultMapper = mock(IndexRelationSearchResultMapper.class);
      indexRequests = mock(IndexRequests.class);
      indexRequestFactory = mock(IndexRequestFactory.class);
      relationSearchParametersConverter = mock(RelationSearchParametersConverter.class);


    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /* Because the RestAutoResourceModule is used in a static way for multiple tests,
   * there should be a way to make sure to the mocks are reset to their default behaviour.
   * This method provides this functionality.
   */
  public void cleanUpMocks() {
    reset(config, repository, userConfigurationHandler, jsonProvider, validator, mailSender, authenticationHandler, broker, indexProducer, persistenceProducer, indexManager, searchRequestValidator,
      searchParametersConverter, relationSearcher, regularClientSearchResultCreator, regularClientSearchResultCreator, basicAuthenticationHandler, changeHelper, vreCollection, autoCompleteResultConverter,
      additionalfilterTranslator, indexRegularSearchResultMapper, indexRelationSearchResultMapper, indexRequests, indexRequestFactory, relationSearchParametersConverter);
  }

  @Override
  protected void configureServlets() {
    bind(DomainEntityResource.class);
    super.configureServlets();
  }

  @Provides
  public Repository providesStorageManager() {
    return repository;
  }

  @Provides
  public TypeRegistry providesDocumentTypeRegister() {
    return typeRegistry;
  }

  @Singleton
  @Provides
  public JacksonJsonProvider providesJsonProvider() {
    return jsonProvider;
  }

  @Provides
  public MailSender providesMailSender() {
    return mailSender;
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
    return validator;
  }

  @Provides
  @Named(value = "security.enabled")
  public boolean provideSecurityEnabled() {
    return true;
  }

  @Provides
  @Singleton
  public Configuration provideConfiguration() {
    return config;
  }

  @Provides
  @Singleton
  public IndexManager provideIndexManager() {
    return indexManager;
  }

  @Provides
  @Singleton
  public SecurityContextCreator provideSecurityContextCreator() {
    return securityContextCreator;
  }

  @Provides
  @Singleton
  public AuthenticationHandler provideAuthenticationHandler() {
    return authenticationHandler;
  }

  @Provides
  @Singleton
  public Broker provideBroker() {
    return broker;
  }

  @Provides
  @Singleton
  @Named("indexProducer")
  public Producer provideIndexProducer() {
    return indexProducer;
  }

  @Provides
  @Singleton
  @Named(PERSISTENCE_PRODUCER)
  public Producer providePersistenceProducer() {
    return persistenceProducer;
  }

  @Singleton
  @Provides
  public VREAuthorizationHandler provideVreAuthorizationHandler() {
    return new DefaultVREAuthorizationHandler(userConfigurationHandler, mailSender);
  }

  @Singleton
  @Provides
  public SearchRequestValidator provideSearchRequestValidator() {
    return searchRequestValidator;
  }

  @Singleton
  @Provides
  public SearchParametersConverter provideSearchParametersConverter() {
    return searchParametersConverter;
  }

  @Singleton
  @Provides
  public RelationSearcher provideRelationSearcher() {
    return relationSearcher;
  }

  @Singleton
  @Provides
  public RegularSearchResultMapper provideRegularClientSearchResultBuilder() {
    return regularClientSearchResultCreator;
  }

  @Singleton
  @Provides
  public RelationSearchResultMapper provideRelationClientSearchResultCreator() {
    return relationClientSearchResultCreator;
  }

  @Singleton
  @Provides
  public UserConfigurationHandler provideJsonFileWriter() {
    return userConfigurationHandler;
  }

  @Singleton
  @Provides
  public BasicAuthenticationHandler provideBasicAuthenticationHandler() {
    return basicAuthenticationHandler;
  }

  @Singleton
  @Provides
  public ChangeHelper provideChangeHelper() {
    return changeHelper;
  }

  @Singleton
  @Provides
  public VRECollection provideVreCollection() {
    return vreCollection;
  }

  @Singleton
  @Provides
  public AutocompleteResultConverter provideAutoCompleteResultConverter() {
    return autoCompleteResultConverter;
  }

  @Singleton
  @Provides
  public AdditionalFilterTranslator provideAdditionalFilterTranslator() {
    return additionalfilterTranslator;
  }

  @Singleton
  @Provides
  public IndexRegularSearchResultMapper provideIndexRegularSearchResultMapper() {
    return indexRegularSearchResultMapper;
  }

  @Singleton
  @Provides
  public IndexRelationSearchResultMapper provideIndexRelationSearchResultMapper() {
    return indexRelationSearchResultMapper;
  }

  @Singleton
  @Provides
  public IndexRequests provideIndexRequestStatus() {
    return indexRequests;
  }

  @Singleton
  @Provides
  public IndexRequestFactory provideIndexRequestFactory() {
    return indexRequestFactory;
  }

  @Singleton
  @Provides
  public RelationSearchParametersConverter provideRelationSearchParametersConverter(){
    return relationSearchParametersConverter;
  }
}
