package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import javax.validation.Validator;

import nl.knaw.huygens.security.client.AuthorizationHandler;
import nl.knaw.huygens.security.client.SecurityContextCreator;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.rest.util.search.RegularClientSearchResultCreator;
import nl.knaw.huygens.timbuctoo.rest.util.search.RelationClientSearchResultCreator;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchRequestValidator;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.search.converters.SearchParametersConverter;
import nl.knaw.huygens.timbuctoo.security.DefaultVREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContextCreator;
import nl.knaw.huygens.timbuctoo.security.VREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

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

  private static final String M0 = "timbuctoo.model";
  private static final String M1 = "test.rest.model";
  private static final String M1A = "test.rest.model.projecta";
  private static final String M1B = "test.rest.model.projectb";
  private static final String PACKAGES = M0 + " " + M1 + " " + M1A + " " + M1B;

  //All the classes are instance variables because we need to be able to reset them after each test.
  private Configuration config;
  private TypeRegistry typeRegistry;
  private Repository repository;
  private JacksonJsonProvider jsonProvider;
  private Validator validator;
  private MailSender mailSender;
  private SecurityContextCreator securityContextCreator;
  private AuthorizationHandler authorizationHandler;
  private Broker broker;
  private Producer indexProducer;
  private Producer persistenceProducer;
  private VREManager vreManager;
  private IndexManager indexManager;
  private SearchRequestValidator searchRequestValidator;
  private SearchParametersConverter searchParametersConverter;
  private RelationSearcher relationSearcher;
  private RegularClientSearchResultCreator regularClientSearchResultCreator;
  private RelationClientSearchResultCreator relationClientSearchResultCreator;

  public ResourceTestModule() {
    try {
      config = mock(Configuration.class);
      typeRegistry = TypeRegistry.getInstance().init(PACKAGES);
      repository = mock(Repository.class);
      jsonProvider = mock(JacksonJsonProvider.class);
      validator = mock(Validator.class);
      mailSender = mock(MailSender.class);
      securityContextCreator = new UserSecurityContextCreator(repository);
      authorizationHandler = mock(AuthorizationHandler.class);
      broker = mock(Broker.class);
      indexProducer = mock(Producer.class);
      persistenceProducer = mock(Producer.class);
      vreManager = mock(VREManager.class);
      indexManager = mock(IndexManager.class);
      searchRequestValidator = mock(SearchRequestValidator.class);
      searchParametersConverter = mock(SearchParametersConverter.class);
      relationSearcher = mock(RelationSearcher.class);
      regularClientSearchResultCreator = mock(RegularClientSearchResultCreator.class);
      relationClientSearchResultCreator = mock(RelationClientSearchResultCreator.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /* Because the RestAutoResourceModule is used in a static way for multiple tests,
   * there should be a way to make sure to the mocks are reset to their default behaviour.
   * This method provides this functionality.
   */
  public void cleanUpMocks() {
    reset(config, repository, jsonProvider, validator, mailSender, authorizationHandler, broker, indexProducer, persistenceProducer, vreManager, indexManager, searchRequestValidator,
        searchParametersConverter, relationSearcher, regularClientSearchResultCreator, regularClientSearchResultCreator);
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
  public AuthorizationHandler provideAuthorizationHandler() {
    return authorizationHandler;
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
  @Named("persistenceProducer")
  public Producer providePersistenceProducer() {
    return persistenceProducer;
  }

  @Singleton
  @Provides
  public VREManager provideVreManager() {
    return vreManager;
  }

  @Singleton
  @Provides
  public VREAuthorizationHandler provideVreAuthorizationHandler() {
    return new DefaultVREAuthorizationHandler(repository, mailSender);
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
  public RegularClientSearchResultCreator provideRegularClientSearchResultBuilder() {
    return regularClientSearchResultCreator;
  }

  @Singleton
  @Provides
  public RelationClientSearchResultCreator provideRelationClientSearchResultCreator() {
    return relationClientSearchResultCreator;
  }

}
