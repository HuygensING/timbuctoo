package nl.knaw.huygens.timbuctoo.rest.config;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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
import nl.knaw.huygens.persistence.PersistenceManagerFactory;
import nl.knaw.huygens.security.client.AuthorizationHandler;
import nl.knaw.huygens.security.client.HuygensAuthorizationHandler;
import nl.knaw.huygens.security.client.SecurityContextCreator;
import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.mail.MailSenderFactory;
import nl.knaw.huygens.timbuctoo.messages.ActiveMQBroker;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContextCreator;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

public class RESTInjectionModule extends BasicInjectionModule {

  public RESTInjectionModule(Configuration config) {
    super(config);
  }

  @Override
  protected void configure() {

    bind(SecurityContextCreator.class).to(UserSecurityContextCreator.class);
    //bind(AuthorizationHandler.class).to(MockAuthorizationHandler.class);
    bind(Broker.class).to(ActiveMQBroker.class);
    super.configure();
  }

  @Provides
  @Singleton
  AuthorizationHandler provideAuthorizationHandler() {
    Client client = new Client();
    return new HuygensAuthorizationHandler(client, config.getSetting("security.hss.url"), config.getSetting("security.hss.credentials"));
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
  PersistenceManager providePersistenceManager() {
    PersistenceManager persistenceManager = PersistenceManagerFactory.newPersistenceManager(config.getBooleanSetting("handle.enabled", true), config.getSetting("handle.cipher"),
        config.getSetting("handle.naming_authority"), config.getSetting("handle.prefix"), config.pathInUserHome(config.getSetting("handle.private_key_file")));
    return persistenceManager;
  }
}
