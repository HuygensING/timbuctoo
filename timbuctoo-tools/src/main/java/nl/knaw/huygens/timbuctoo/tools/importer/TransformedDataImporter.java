package nl.knaw.huygens.timbuctoo.tools.importer;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This importer uses json-files created by the {@code BulkDataTransformer}, to import into a database.
 * This json-files are named like {internal class name}.json. The structure of the classes in these 
 * files should be the same as the structure that is communicated with client applications.
 *
 */
public class TransformedDataImporter extends DefaultImporter {

  private static final Logger LOG = LoggerFactory.getLogger(TransformedDataImporter.class);

  public static void main(String[] args) throws ConfigurationException, ClassNotFoundException, IndexException, JsonParseException, JsonMappingException, IOException {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    IndexManager indexManager = injector.getInstance(IndexManager.class);
    TypeRegistry registry = injector.getInstance(TypeRegistry.class);

    String dataPath = args.length > 0 ? args[0] : "src/main/resources/testdata";
    new TransformedDataImporter(storageManager, registry, indexManager).importData(dataPath);
  }

  public TransformedDataImporter(StorageManager storageManager, TypeRegistry typeRegistry, IndexManager indexManager) {
    super(typeRegistry, storageManager, indexManager);
  }

  protected void importData(String dataPath) throws IOException, IndexException, JsonParseException, JsonMappingException {
    Change change = new Change("timbuctoo", "timbuctoo");

    File[] jsonFiles = getJsonFiles(dataPath);

    for (File jsonFile : jsonFiles) {
      String className = jsonFile.getName().substring(0, jsonFile.getName().indexOf('.'));
      Class<? extends Entity> type = typeRegistry.getTypeForIName(className);

      if (TypeRegistry.isDomainEntity(type)) {
        super.removeNonPersistentEntities(TypeRegistry.toDomainEntity(type));
        save(TypeRegistry.toDomainEntity(type), jsonFile, change);
      } else {
        LOG.error("{} is not a DomainEntity.", className);
      }
    }

    storageManager.close();
    indexManager.close();
  }

  protected File[] getJsonFiles(String dataPath) {
    File dataDir = new File(dataPath);
    File[] jsonFiles = dataDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(".json");
      }
    });
    return jsonFiles;
  }

  public <T extends DomainEntity> void save(Class<T> type, File jsonFile, Change change) throws JsonParseException, JsonMappingException, IOException, IndexException {
    LOG.info("Saving for type {}", type);
    List<T> entities = new ObjectMapper().readValue(jsonFile, new TypeReference<List<? extends DomainEntity>>() {});
    for (T entity : entities) {
      String id = storageManager.addDomainEntity(type, entity, change);
      indexManager.addEntity(type, id);
    }
  }
}
