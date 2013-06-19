package nl.knaw.huygens.repository;

import javax.servlet.ServletContextEvent;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.ServletInjectionModule;
import nl.knaw.huygens.repository.managers.StorageManager;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Creates and manages the injector for the repository servlet.
 */
public class RepoContextListener extends GuiceServletContextListener {

  private Injector injector;

  @Override
  protected Injector getInjector() {
    try {
      Configuration config = new Configuration();
      Module baseModule = new BasicInjectionModule(config);
      Module servletModule = new ServletInjectionModule();
      injector = Guice.createInjector(baseModule, servletModule);
      return injector;
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    if (injector != null) {
      StorageManager storageManager = injector.getInstance(StorageManager.class);
      if (storageManager != null) {
        storageManager.close();
      }
      injector = null;
    }
  }

}
