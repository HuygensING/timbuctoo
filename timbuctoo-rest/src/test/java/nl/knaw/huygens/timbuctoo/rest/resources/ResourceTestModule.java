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
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.security.DefaultVREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContextCreator;
import nl.knaw.huygens.timbuctoo.security.VREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
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
  private static final String M1 = "timbuctoo.rest.model";
  private static final String M1A = "timbuctoo.rest.model.projecta";
  private static final String M1B = "timbuctoo.rest.model.projectb";
  private static final String PACKAGES = M0 + " " + M1 + " " + M1A + " " + M1B;

  //All the classes are instance variables because we need to be able to reset them after each test.
  private Configuration config;
  private TypeRegistry typeRegistry;
  private StorageManager storageManager;
  private JacksonJsonProvider jsonProvider;
  private Validator validator;
  private MailSender mailSender;
  private SearchManager searchManager;
  private SecurityContextCreator securityContextCreator;
  private AuthorizationHandler authorizationHandler;
  private Broker broker;
  private Producer indexProducer;
  private Producer persistenceProducer;
  private VREManager vreManager;

  public ResourceTestModule() {
    config = mock(Configuration.class);
    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init(PACKAGES);
    storageManager = mock(StorageManager.class);
    jsonProvider = mock(JacksonJsonProvider.class);
    validator = mock(Validator.class);
    mailSender = mock(MailSender.class);
    searchManager = mock(SearchManager.class);
    securityContextCreator = new UserSecurityContextCreator(storageManager);
    authorizationHandler = mock(AuthorizationHandler.class);
    broker = mock(Broker.class);
    indexProducer = mock(Producer.class);
    persistenceProducer = mock(Producer.class);
    vreManager = mock(VREManager.class);
  }

  /* Because the RestAutoResourceModule is used in a static way for multiple tests,
   * there should be a way to make sure to the mocks are reset to their default behaviour.
   * This method provides this functionality.
   */
  public void cleanUpMocks() {
    reset(config, storageManager, jsonProvider, validator, mailSender, searchManager, authorizationHandler, broker, indexProducer, persistenceProducer, vreManager);
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
  public TypeRegistry providesDocumentTypeRegister() {
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
  public Configuration provideConfiguration() {
    return config;
  }

  @Provides
  @Singleton
  public SearchManager provideSearchManager() {
    return searchManager;
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
    return this.broker;
  }

  @Provides
  @Singleton
  @Named("indexProducer")
  public Producer provideIndexProducer() {
    return this.indexProducer;
  }

  @Provides
  @Singleton
  @Named("persistenceProducer")
  public Producer providePersistenceProducer() {
    return this.persistenceProducer;
  }

  @Singleton
  @Provides
  public VREManager provideVreManager() {
    return this.vreManager;
  }
  
  @Singleton
  @Provides
  public VREAuthorizationHandler provideVreAuthorizationHandler(){
    return new DefaultVREAuthorizationHandler(this.mailSender, this.storageManager);
  }
}
