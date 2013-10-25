package nl.knaw.huygens.timbuctoo.rest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.servlet.ServletContextEvent;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexService;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceService;
import nl.knaw.huygens.timbuctoo.rest.config.RESTInjectionModule;
import nl.knaw.huygens.timbuctoo.rest.config.ServletInjectionModule;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.util.TimeUtils;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Creates and manages the injector for the repository servlet.
 */
public class RepoContextListener extends GuiceServletContextListener {

  // ** Note **
  // After stopping Tomcat one gets a message
  // "SEVERE: The web application appears to have started a thread named [com.google.inject.internal.util.$Finalizer]
  // but has failed to stop it. This is very likely to create a memory leak."
  // According to the Guice project this is just the eager Tomcat detector warning about a potential leak based
  // on it's internal introspection. Once new ThreadLocals are created the entry that causes this message is removed.
  // See: http://code.google.com/p/google-guice/issues/detail?id=707

  private Configuration config;
  private Injector injector;
  private Broker broker;
  private IndexService indexService;
  private Thread indexServiceThread;
  private PersistenceService persistenceService;
  private ExecutorService persistenceThreadExecutor;
  private RepoScheduler scheduler;

  @Override
  protected Injector getInjector() {
    try {
      config = new Configuration();
      Module baseModule = new RESTInjectionModule(config);
      Module servletModule = new ServletInjectionModule();
      injector = Guice.createInjector(baseModule, servletModule);
      return injector;
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    super.contextInitialized(event);

    if (config.getBooleanSetting("search_results_cleanup.enabled", false)) {
      long interval = getMillis("search_results_cleanup.interval", "00:15:00");
      long ttl = getMillis("search_results_cleanup.ttl", "24:00:00");
      scheduler = new RepoScheduler(injector, interval, ttl);
      scheduler.start();
    }

    try {
      broker = injector.getInstance(Broker.class);
      broker.start();
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }

    indexService = injector.getInstance(IndexService.class);
    indexServiceThread = new Thread(indexService);
    indexServiceThread.start();

    persistenceService = injector.getInstance(PersistenceService.class);
    persistenceThreadExecutor = Executors.newSingleThreadExecutor();
    persistenceThreadExecutor.execute(persistenceService);
  }

  private long getMillis(String key, String defaultValue) {
    String text = config.getSetting(key, defaultValue);
    long value = TimeUtils.hhmmssToMillis(text);
    if (value < 0) {
      throw new RuntimeException("Invalid configuration value for " + key);
    }
    return value;
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    if (scheduler != null) {
      scheduler.stop();
      scheduler = null;
    }
    if (indexServiceThread != null) {
      indexService.stop();
      IndexService.waitForCompletion(indexServiceThread, 1 * 1000);
      indexServiceThread = null;
    }
    if (injector != null) {
      StorageManager storageManager = injector.getInstance(StorageManager.class);
      if (storageManager != null) {
        storageManager.close();
      }
      injector = null;
    }

    if (persistenceThreadExecutor != null) {
      persistenceService.stop();
      persistenceThreadExecutor.shutdown();

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      if (!persistenceThreadExecutor.isTerminated()) {
        //force shutdown
        persistenceThreadExecutor.shutdownNow();
      }
    }

    if (broker != null) {
      broker.close();
      broker = null;
    }
    super.contextDestroyed(event);
  }

}
