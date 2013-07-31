package nl.knaw.huygens.repository.config;

import javax.validation.Validation;
import javax.validation.Validator;

import nl.knaw.huygens.repository.mail.MailSender;
import nl.knaw.huygens.repository.mail.MailSenderFactory;
import nl.knaw.huygens.repository.persistence.PersistenceManager;
import nl.knaw.huygens.repository.persistence.PersistenceManagerFactory;
import nl.knaw.huygens.repository.search.FacetFinder;
import nl.knaw.huygens.repository.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.repository.search.SortableFieldFinder;
import nl.knaw.huygens.repository.storage.VariationStorage;
import nl.knaw.huygens.repository.storage.mongo.variation.MongoStorageFacade;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class BasicInjectionModule extends AbstractModule {

  private final Configuration config;
  private final DocTypeRegistry registry;

  public BasicInjectionModule(Configuration config) {
    this.config = config;
    registry = new DocTypeRegistry(config.getSetting("model-packages"));
    new ConfigValidator(config, registry).validate();
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), config.getAll());
    bind(Configuration.class).toInstance(config);
    bind(DocTypeRegistry.class).toInstance(registry);

    bind(VariationStorage.class).to(MongoStorageFacade.class);
  }

  @Provides
  @Singleton
  PersistenceManager providePersistenceManager() {
    return PersistenceManagerFactory.newPersistenceManager(config);
  }

  @Provides
  @Singleton
  Validator provideValidator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Provides
  @Singleton
  MailSender provideMailSender() {
    return new MailSenderFactory(config).create();
  }

  @Provides
  @Singleton
  FacetFinder provideFacetFinder() {
    return new FacetFinder();
  }

  @Provides
  @Singleton
  FullTextSearchFieldFinder provideFullTextSearchFieldFinder() {
    return new FullTextSearchFieldFinder();
  }

  @Provides
  @Singleton
  SortableFieldFinder provideSortableFieldFinder() {
    return new SortableFieldFinder();
  }
}
