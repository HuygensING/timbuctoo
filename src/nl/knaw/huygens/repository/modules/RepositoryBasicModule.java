package nl.knaw.huygens.repository.modules;

import javax.validation.Validation;
import javax.validation.Validator;

import nl.knaw.huygens.repository.persistence.DefaultPersistenceManager;
import nl.knaw.huygens.repository.persistence.PersistenceManager;
import nl.knaw.huygens.repository.persistence.handle.HandleManager;
import nl.knaw.huygens.repository.server.security.NoSecurityOAuthAuthorizationServerConnector;
import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;
import nl.knaw.huygens.repository.server.security.apis.ApisAuthorizationServerConnector;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.util.Configuration;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class RepositoryBasicModule extends AbstractModule {

  private final Configuration config;

  public RepositoryBasicModule(String configPath) {
    try {
      config = new Configuration(configPath);
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), config.getAll());
    bind(Configuration.class).toInstance(config);
    if (config.getBooleanSetting("use-security")) {
      bind(OAuthAuthorizationServerConnector.class).to(ApisAuthorizationServerConnector.class);
    } else {
      bind(OAuthAuthorizationServerConnector.class).to(NoSecurityOAuthAuthorizationServerConnector.class);
    }

    Class<? extends Storage> cls = new StorageConfiguration(config).getType().getCls();
    bind(Storage.class).to(cls);
  }

  @Provides
  @Singleton
  PersistenceManager providePersistenceManager() {
    if (config.getBooleanSetting("use-handle-system", true)) {
      return HandleManager.newHandleManager(config);
    } else {
      return new DefaultPersistenceManager();
    }
  }

  @Provides
  @Singleton
  Validator provideValidator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

}
