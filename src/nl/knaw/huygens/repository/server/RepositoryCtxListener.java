package nl.knaw.huygens.repository.server;

import nl.knaw.huygens.repository.modules.RepositoryBasicModule;
import nl.knaw.huygens.repository.modules.RepositoryServletModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class RepositoryCtxListener extends GuiceServletContextListener {

  private static RepositoryServletModule servletModule;
  private static RepositoryBasicModule module;
  private static Injector injector;

  public static Injector getMyInjector() {
    return injector;
  }

  @Override
  protected Injector getInjector() {
    synchronized (this) {
      if (injector == null) {
        if (module == null) {
          module = new RepositoryBasicModule("../config.xml");
        }
        if (servletModule == null) {
          servletModule = new RepositoryServletModule();
        }
        injector = Guice.createInjector(module, servletModule);
      }
      return injector;
    }
  }

}
