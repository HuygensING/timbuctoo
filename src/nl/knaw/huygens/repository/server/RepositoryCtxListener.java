package nl.knaw.huygens.repository.server;

import java.util.Map;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.util.Configuration;

import org.apache.commons.configuration.ConfigurationException;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class RepositoryCtxListener extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new JerseyServletModule() {
      @Override
      protected void configureServlets() {
        Configuration conf;
        try {
          conf = new Configuration("../config.xml");
        } catch (ConfigurationException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        StorageManager storageManager = new StorageManager(conf);
        bind(StorageManager.class).toInstance(storageManager);
        Map<String, String> params = Maps.newHashMap();
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.repository.resources");
        serve("/repository/*").with(GuiceContainer.class, params);
      }
    });
  }

}
