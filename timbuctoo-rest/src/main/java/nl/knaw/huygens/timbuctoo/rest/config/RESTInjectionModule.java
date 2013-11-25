package nl.knaw.huygens.timbuctoo.rest.config;

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
    return new HuygensAuthorizationHandler(client, config.getSetting("security.hss.url"));
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
