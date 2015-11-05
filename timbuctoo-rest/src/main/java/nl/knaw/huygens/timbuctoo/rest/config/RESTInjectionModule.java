package nl.knaw.huygens.timbuctoo.rest.config;

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

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;
import nl.knaw.huygens.persistence.PersistenceManagerFactory;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.HuygensAuthenticationHandler;
import nl.knaw.huygens.security.client.SecurityContextCreator;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.solr.AbstractSolrServerBuilderProvider;
import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexFacade;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.solr.SolrIndexFactory;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.mail.MailSenderFactory;
import nl.knaw.huygens.timbuctoo.messages.ActiveMQBroker;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.security.DefaultVREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.security.ExampleAuthenticationHandler;
import nl.knaw.huygens.timbuctoo.security.ExampleVREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.security.SecurityType;
import nl.knaw.huygens.timbuctoo.security.TimbuctooAuthenticationHandler;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContextCreator;
import nl.knaw.huygens.timbuctoo.security.VREAuthorizationHandler;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import nl.knaw.huygens.timbuctoo.vre.VREs;

import javax.validation.Validation;
import javax.validation.Validator;

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
    bind(VRECollection.class).to(VREs.class);

    bind(SecurityContextCreator.class).to(UserSecurityContextCreator.class);
    bind(Broker.class).to(ActiveMQBroker.class);
    bind(IndexManager.class).to(IndexFacade.class);

    if (SecurityType.DEFAULT.equals(securityType)) {
      bind(AuthenticationHandler.class).to(TimbuctooAuthenticationHandler.class);
      bind(VREAuthorizationHandler.class).to(DefaultVREAuthorizationHandler.class);
    } else {
      bind(AuthenticationHandler.class).to(ExampleAuthenticationHandler.class);
      bind(VREAuthorizationHandler.class).to(ExampleVREAuthorizationHandler.class);
    }
  }

  @Override
  protected void validateConfig(Configuration config) {
    new RestConfigValidator(config).validate();
  }

  @Provides
  @Singleton
  HuygensAuthenticationHandler provideAuthenticationHandler() {
    Client client = new Client();
    String url = config.getSetting("security.hss.url");
    String credentials = config.getSetting("security.hss.credentials");
    return new HuygensAuthenticationHandler(client, url, credentials);
  }

  @Provides
  @Singleton
  Validator provideValidator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Provides
  @Singleton
  MailSender provideMailSender() {
    boolean enabled = config.getBooleanSetting("mail.enabled");
    String host = config.getSetting("mail.host");
    String port = config.getSetting("mail.port");
    String fromAddress = config.getSetting("mail.from_address");
    return new MailSenderFactory(enabled, host, port, fromAddress).create();
  }

  @Provides
  @Singleton
  PersistenceManager providePersistenceManager() throws PersistenceManagerCreationException {
    boolean enabled = config.getBooleanSetting("handle.enabled", true);
    String cipher = config.getSetting("handle.cipher");
    String authority = config.getSetting("handle.naming_authority");
    String prefix = config.getSetting("handle.prefix");
    String pathToPrivateKey = config.pathInUserHome(config.getSetting("handle.private_key_file"));
    return PersistenceManagerFactory.newPersistenceManager(enabled, cipher, authority, prefix, pathToPrivateKey);
  }

}
