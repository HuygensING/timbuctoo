package nl.knaw.huygens.repository.server;

import nl.knaw.huygens.repository.modules.RepositoryBasicModule;
import nl.knaw.huygens.repository.modules.RepositoryServletModule;

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
    Module baseModule = new RepositoryBasicModule("../config.xml");
    Module servletModule = new RepositoryServletModule();
    return Guice.createInjector(baseModule, servletModule);
  }

}
