package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.MongoAdmin;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TransformedDataImporter {
  private static final Logger LOG = LoggerFactory.getLogger(TransformedDataImporter.class);

  public static void main(String[] args) throws ConfigurationException, ClassNotFoundException, IndexException, JsonParseException, JsonMappingException, IOException {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    // TODO Remove non-persistent items only
    MongoAdmin admin = new MongoAdmin(config);
    admin.dropDatabase();

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    IndexManager indexManager = injector.getInstance(IndexManager.class);
    TypeRegistry registry = injector.getInstance(TypeRegistry.class);
    indexManager.deleteAllEntities();

    String dataPath = args.length > 0 ? args[0] : "src/main/resources/testdata";
    File dataDir = new File(dataPath);
    File[] jsonFiles = dataDir.listFiles(new FileFilter() {

      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(".json");
      }
    });

    for (File jsonFile : jsonFiles) {
      String className = jsonFile.getName().substring(0, jsonFile.getName().indexOf('.'));

      Class<? extends Entity> type = registry.getTypeForIName(className.toLowerCase());
      save(type, jsonFile, storageManager, indexManager);

    }

    storageManager.close();
    indexManager.close();

  }

  public static <T extends Entity> void save(Class<T> type, File jsonFile, StorageManager storageManager, IndexManager indexManager) throws JsonParseException, JsonMappingException, IOException,
      IndexException {
    LOG.info("Saving for type {}", type);
    List<T> entities = new ObjectMapper().readValue(jsonFile, new TypeReference<List<? extends Entity>>() {});
    for (T entity : entities) {
      String id = storageManager.addEntity(type, entity);
      indexManager.addEntity(type, id);
    }
  }
}
