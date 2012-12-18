package nl.knaw.huygens.repository.server;

import java.util.Map;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.util.Configuration;

import org.apache.commons.configuration.ConfigurationException;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

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
        Hub hub = new Hub();
        StorageManager storageManager = new StorageManager(conf, hub);
        bind(StorageManager.class).toInstance(storageManager);
        bind(Hub.class).toInstance(hub);
        Names.bindProperties(this.binder(), conf.getAll());
        Map<String, String> params = Maps.newHashMap();
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.repository.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.repository.providers");
        params.put(PackagesResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, "com.sun.jersey.api.container.filter.LoggingFilter");
        params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, ".*static.*");
        params.put(ServletContainer.FEATURE_FILTER_FORWARD_ON_404, "true");
        filter("/*").through(GuiceContainer.class, params);
      }
    });
  }

}
