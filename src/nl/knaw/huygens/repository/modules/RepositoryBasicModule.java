package nl.knaw.huygens.repository.modules;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.name.Names;

import nl.knaw.huygens.repository.pubsub.Hub;
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
    Hub hub = new Hub();
    bind(Hub.class).toInstance(hub);
    bind(Configuration.class).toInstance(conf);
    configureStorage(conf);
    ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
    Validator validator = vf.getValidator();
    bind(Validator.class).toInstance(validator);
  }
  
  private void configureStorage(Configuration conf) {
    Class<? extends Storage> cls = new StorageConfiguration(conf).getType().getCls();
    bind(Storage.class).to(cls);
  }
  
  public Binder getBinder() {
    return binder();
  }
}
