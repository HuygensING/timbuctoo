package nl.knaw.huygens.repository.rest.config;

import javax.validation.Validation;
import javax.validation.Validator;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.messages.ActiveMQBroker;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.security.UserSecurityContextCreator;
import nl.knaw.huygens.repository.services.mail.MailSender;
import nl.knaw.huygens.repository.services.mail.MailSenderFactory;
import nl.knaw.huygens.security.AuthorizationHandler;
import nl.knaw.huygens.security.SecurityContextCreator;
import nl.knaw.huygens.security.apis.ApisAuthorizationHandler;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class RESTInjectionModule extends BasicInjectionModule {

  public RESTInjectionModule(Configuration config) {
    super(config);
  }

  @Override
  protected void configure() {

    bind(SecurityContextCreator.class).to(UserSecurityContextCreator.class);
    bind(AuthorizationHandler.class).to(ApisAuthorizationHandler.class);
    bind(Broker.class).to(ActiveMQBroker.class);
    super.configure();
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
}
