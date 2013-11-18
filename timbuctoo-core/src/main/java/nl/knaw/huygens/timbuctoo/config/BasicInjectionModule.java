package nl.knaw.huygens.timbuctoo.config;

import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorageFacade;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class BasicInjectionModule extends AbstractModule {

  protected final Configuration config;
  private final TypeRegistry registry;

  public BasicInjectionModule(Configuration config) {
    this.config = config;
    registry = new TypeRegistry(config.getSetting("model-packages"));
    new ConfigValidator(config, registry).validate();
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), config.getAll());
    bind(Configuration.class).toInstance(config);
    bind(TypeRegistry.class).toInstance(registry);

    bind(Storage.class).to(MongoStorageFacade.class);
  }

}