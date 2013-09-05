package nl.knaw.huygens.repository.rest.config;

import javax.validation.Validation;
import javax.validation.Validator;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.rest.mail.MailSender;
import nl.knaw.huygens.repository.rest.mail.MailSenderFactory;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class RESTInjectionModule extends BasicInjectionModule {

  public RESTInjectionModule(Configuration config) {
    super(config);
  }

  //REST only
  @Provides
  @Singleton
  Validator provideValidator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

  //REST only
  @Provides
  @Singleton
  MailSender provideMailSender() {
    return new MailSenderFactory(config).create();
  }
}
