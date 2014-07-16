package nl.knaw.huygens.timbuctoo.tools.importer;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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
import java.util.Map;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.util.Progress;
import nl.knaw.huygens.timbuctoo.util.Files;

import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

/**
 * Imports json files from a directory.
 * The import directory must contain a file "import.txt" specifying the json files to read.
 * <p>
 * Usage:<pre>
 * java nl.knaw.huygens.timbuctoo.tools.importer.JsonImporter  [ vreId [directory] ]
 * </pre>
 */
public class JsonImporter extends CSVImporter {

  private static final Logger LOG = LoggerFactory.getLogger(JsonImporter.class);

  private static final String DEFAULT_VRE_ID = "base";
  private static final String IMPORT_DIRECTORY_NAME = "../../timbuctoo-testdata/src/main/resources/import/";
  private static final String CONTROL_FILE_NAME = "import.txt";

  public static void main(String[] args) throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();

    String vreId = (args.length > 0) ? args[0] : DEFAULT_VRE_ID;
    LOG.info("VRE: {}", vreId);

    String directoryName = (args.length > 1) ? args[1] : IMPORT_DIRECTORY_NAME + vreId + "/";
    LOG.info("Import directory: {}", directoryName);

    Injector injector = ToolsInjectionModule.createInjector();
    Repository repository = injector.getInstance(Repository.class);
    IndexManager indexManager = injector.getInstance(IndexManager.class);

    try {
      File directory = new File(directoryName);
      File file = new File(directory, CONTROL_FILE_NAME);
      JsonImporter importer = new JsonImporter(repository, indexManager, vreId, directory);
      importer.handleFile(file, 2, false);
    } finally {
      indexManager.close();
      repository.close();
      LOG.info("Time used: {}", stopWatch);
    }
  }

  // ---------------------------------------------------------------------------

  private final TypeRegistry registry;
  private final File directory;
  private final Handler handler;

  public JsonImporter(Repository repository, IndexManager indexManager, String vreId, File directory) {
    registry = repository.getTypeRegistry();
    this.directory = directory;
    handler = new Handler(repository, indexManager, vreId);
  }

  @Override
  protected void handleLine(String[] items) throws Exception {
    File file = new File(directory, items[0]);
    Class<? extends DomainEntity> type = registry.getDomainEntityType(items[1]);
    handler.handleFile(file, type);
  }

  @Override
  protected void handleEndOfFile() throws Exception {
    handler.indexEntitiesWithRelations();
    handler.displayErrorSummary();
    handler.displayStatus();
  };

  // ---------------------------------------------------------------------------

  private static class Handler extends DefaultImporter {

    /** References of stored primitive entities */
    private final Map<String, Reference> references;
    private final ObjectMapper mapper;

    public Handler(Repository repository, IndexManager indexManager, String vreId) {
      super(repository, indexManager, vreId);
      references = Maps.newHashMap();
      mapper = new ObjectMapper();
    }

    private void storeReference(String key, Class<? extends DomainEntity> type, String id) {
      Reference reference = new Reference(TypeRegistry.toBaseDomainEntity(type), id);
      if (references.put(key, reference) != null) {
        log("Duplicate key '%s'%n", key);
        System.exit(-1);
      }
    }

    public <T extends DomainEntity> void handleFile(File file, Class<T> type) throws Exception {
      printBoxedText("File: " + file.getName());
      String prefix = String.format("{\"@type\":\"%s\",", TypeNames.getInternalName(type));

      // Get rid of existing stuff
      removeNonPersistentEntities(type);

      Progress progress = new Progress();
      LineIterator iterator = Files.getLineIterator(file);
      try {
        while (iterator.hasNext()) {
          String line = iterator.nextLine().trim();
          if (!line.isEmpty()) {
            progress.step();
            line = prefix + line.substring(1);
            storeEntity(type, line);
            // FIX all data is required before indexing
            // indexManager.addEntity(type, storedId);
          }
        }
      } finally {
        LineIterator.closeQuietly(iterator);
        progress.done();
      }
    }

    private <T extends DomainEntity> void storeEntity(Class<T> type, String line) throws Exception {
      T entity = mapper.readValue(line, type);
      String importId = entity.getId();
      String storedId = addDomainEntity(type, entity);
      if (importId != null) {
        storeReference(importId, type, storedId);
      } else {
        // has no new relations, index immediately
        indexManager.addEntity(type, storedId);
      }
    }

    private void indexEntitiesWithRelations() throws Exception {
      if (!references.isEmpty()) {
        printBoxedText("Index entities");
        TypeRegistry registry = repository.getTypeRegistry();
        Progress progress = new Progress();
        try {
          for (Reference reference : references.values()) {
            progress.step();
            String iname = reference.getType();
            Class<? extends DomainEntity> type = registry.getDomainEntityType(iname);
            indexManager.addEntity(type, reference.getId());
          }
        } finally {
          progress.done();
        }
      }
    }
  }

}
