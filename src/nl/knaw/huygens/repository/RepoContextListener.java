package nl.knaw.huygens.repository;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Creates the injector for the repository servlet.
 */
public class RepoContextListener extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    Configuration config = getConfiguration();
    Module baseModule = new BasicInjectionModule(config);
    Module servletModule = new ServletInjectionModule();
    return Guice.createInjector(baseModule, servletModule);
  }

  private Configuration getConfiguration() {
    try {
      return new Configuration(Configuration.DEFAULT_CONFIG_FILE);
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

}
