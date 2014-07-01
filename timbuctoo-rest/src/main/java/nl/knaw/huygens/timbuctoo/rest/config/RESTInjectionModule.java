package nl.knaw.huygens.timbuctoo.rest.config;

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

import javax.validation.Validation;
import javax.validation.Validator;

import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;
import nl.knaw.huygens.persistence.PersistenceManagerFactory;
import nl.knaw.huygens.security.client.AuthorizationHandler;
import nl.knaw.huygens.security.client.HuygensAuthorizationHandler;
import nl.knaw.huygens.security.client.SecurityContextCreator;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.solr.AbstractSolrServerBuilderProvider;
import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexFacade;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.SolrIndexFactory;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.mail.MailSenderFactory;
import nl.knaw.huygens.timbuctoo.messages.ActiveMQBroker;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.search.MongoRelationSearcher;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.security.DefaultVREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.security.ExampleAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.security.ExampleVREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.security.SecurityType;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContextCreator;
import nl.knaw.huygens.timbuctoo.security.VREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.vre.VREManager;
import nl.knaw.huygens.timbuctoo.vre.VREManagerProvider;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

public class RESTInjectionModule extends BasicInjectionModule {
  private final SecurityType securityType;

  public RESTInjectionModule(Configuration config) {
    super(config);
    securityType = SecurityType.getFromString(config.getSetting("security.type"));
  }

  @Override
  protected void configure() {

    super.configure();

    bind(AbstractSolrServerBuilder.class).toProvider(AbstractSolrServerBuilderProvider.class);
    bind(IndexFactory.class).to(SolrIndexFactory.class);
    bind(VREManager.class).toProvider(VREManagerProvider.class).in(Singleton.class);

    bind(SecurityContextCreator.class).to(UserSecurityContextCreator.class);
    bind(Broker.class).to(ActiveMQBroker.class);
    bind(SearchManager.class).to(IndexFacade.class);
    bind(IndexManager.class).to(IndexFacade.class);
    bind(RelationSearcher.class).to(MongoRelationSearcher.class);

    configureTheAuthorizationHandler();
  }

  private void configureTheAuthorizationHandler() {
    if (SecurityType.DEFAULT.equals(securityType)) {
      bind(VREAuthorizationHandler.class).to(DefaultVREAuthorizationHandler.class);
    } else {
      bind(VREAuthorizationHandler.class).to(ExampleVREAuthorizationHandler.class);
    }
  }

  @Provides
  @Singleton
  AuthorizationHandler provideAuthorizationHandler() {
    if (SecurityType.DEFAULT.equals(securityType)) {
      Client client = new Client();
      return new HuygensAuthorizationHandler(client, config.getSetting("security.hss.url"), config.getSetting("security.hss.credentials"));
    }

    return new ExampleAuthorizationHandler();
  }

  @Provides
  @Singleton
  Validator provideValidator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Provides
  @Singleton
  MailSender provideMailSender() {
    return new MailSenderFactory(config.getBooleanSetting("mail.enabled"), config.getSetting("mail.host"), config.getSetting("mail.port"), config.getSetting("mail.from_address")).create();
  }

  @Provides
  @Singleton
  PersistenceManager providePersistenceManager() throws PersistenceManagerCreationException {
    PersistenceManager persistenceManager = PersistenceManagerFactory.newPersistenceManager(config.getBooleanSetting("handle.enabled", true), config.getSetting("handle.cipher"),
        config.getSetting("handle.naming_authority"), config.getSetting("handle.prefix"), config.pathInUserHome(config.getSetting("handle.private_key_file")));
    return persistenceManager;
  }
}
