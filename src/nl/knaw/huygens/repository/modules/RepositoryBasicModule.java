package nl.knaw.huygens.repository.modules;

import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import nl.knaw.huygens.repository.persistence.PersistenceManager;
import nl.knaw.huygens.repository.persistence.handle.HandleManager;
import nl.knaw.huygens.repository.persistence.handle.HandleManagerFactory;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.util.Configuration;

public class RepositoryBasicModule extends AbstractModule {
  private Configuration conf;
  
  public RepositoryBasicModule(String configurationPath) {
    try {
      conf = new Configuration(configurationPath);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void configure() {
    Names.bindProperties(this.binder(), conf.getAll());
    bind(Configuration.class).toInstance(conf);
    
    bind(PersistenceManager.class).to(HandleManager.class);
    
    configureStorage(conf);
  }
  
  private void configureStorage(Configuration conf) {
    Class<? extends Storage> cls = new StorageConfiguration(conf).getType().getCls();
    bind(Storage.class).to(cls);
  }
  
  @Provides @Singleton
  HandleManager provideHandleManager() {
    return new HandleManagerFactory(conf).createPersistenceManager();
  }
  
  @Provides @Singleton
  Validator provideValidator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }
  
  public Binder getBinder() {
    return binder();
  }
}
