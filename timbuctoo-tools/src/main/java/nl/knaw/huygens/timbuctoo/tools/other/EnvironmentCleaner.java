package nl.knaw.huygens.timbuctoo.tools.other;

import java.net.UnknownHostException;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * A class to clean up the database and the index.
 */
public class EnvironmentCleaner {
  public static void main(String[] args) throws ConfigurationException, UnknownHostException, IndexException {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    // clean the database.
    MongoAdmin admin = new MongoAdmin(config);
    admin.dropDatabase();

    // clean the index.
    IndexManager indexManager = injector.getInstance(IndexManager.class);
    indexManager.deleteAllEntities();

    indexManager.close();
  }
}
