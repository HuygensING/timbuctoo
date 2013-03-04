package nl.knaw.huygens.repository.server;

import java.util.Map;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.configuration.ConfigurationException;

import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.util.Configuration;

public class RepositoryCtxListener extends GuiceServletContextListener {
  public static class RepositoryModule extends JerseyServletModule {
    @Override
    protected void configureServlets() {
      Configuration conf;
      try {
        conf = new Configuration("../config.xml");
      } catch (ConfigurationException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      Names.bindProperties(this.binder(), conf.getAll());
      Hub hub = new Hub();
      bind(Hub.class).toInstance(hub);
      bind(Configuration.class).toInstance(conf);
      configureStorage(conf);
      
      ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
      Validator validator = vf.getValidator();
      bind(Validator.class).toInstance(validator);
      
      Map<String, String> params = Maps.newHashMap();
      params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.repository.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.repository.providers");
      params.put(PackagesResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, "com.sun.jersey.api.container.filter.LoggingFilter");
      params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, "/static.*");
      params.put(ServletContainer.FEATURE_FILTER_FORWARD_ON_404, "true");
      filter("/*").through(GuiceContainer.class, params);
    }
    private void configureStorage(Configuration conf) {
      Class<? extends Storage> cls = new StorageConfiguration(conf).getType().getCls();
      bind(Storage.class).to(cls);
    }
    public Binder getBinder() {
      return binder();
    }
  }

  private static RepositoryModule module;
  private static Injector injector;
  public static RepositoryModule getModule() {
    return module;
  }
  
  public static Injector getMyInjector() {
    return injector;
  }

  @Override
  protected Injector getInjector() {
    if (injector == null) {
      if (module == null) {
        module = new RepositoryModule();
      }
      injector = Guice.createInjector(module);
    }
    return injector;
  }

}
