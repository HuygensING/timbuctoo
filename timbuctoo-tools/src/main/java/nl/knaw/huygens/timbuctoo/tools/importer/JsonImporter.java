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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.util.Progress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

public class JsonImporter extends CSVImporter {

  private static final String CONTROL_FILE_NAME = "import.txt";

  public static void main(String[] args) throws Exception {
    String directoryName = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/import/base/";
    File directory = new File(directoryName);

    Injector injector = ToolsInjectionModule.createInjector();
    Repository repository = injector.getInstance(Repository.class);
    IndexManager indexManager = injector.getInstance(IndexManager.class);

    try {
      File file = new File(directory, CONTROL_FILE_NAME);
      JsonImporter importer = new JsonImporter(repository, indexManager, directory);
      importer.handleFile(file, 2, false);
    } finally {
      indexManager.commitAll();
      indexManager.close();
      repository.close();
    }
  }

  // ---------------------------------------------------------------------------

  private final TypeRegistry registry;
  private final File directory;
  private final Handler handler;

  public JsonImporter(Repository repository, IndexManager indexManager, File directory) {
    registry = repository.getTypeRegistry();
    this.directory = directory;
    Change change = new Change("importer", "base");
    handler = new Handler(repository, indexManager, change);
  }

  @Override
  protected void handleLine(String[] items) throws Exception {
    File file = new File(directory, items[0]);
    Class<? extends DomainEntity> type = registry.getDomainEntityType(items[1]);
    handler.handleFile(file, type);
  }

  // ---------------------------------------------------------------------------

  private static class Handler extends DefaultImporter {

    private final Change change;
    private final ObjectMapper mapper;

    public Handler(Repository repository, IndexManager indexManager, Change change) {
      super(repository, indexManager);
      this.change = change;
      mapper = new ObjectMapper();
    }

    public <T extends DomainEntity> void handleFile(File file, Class<T> type) throws Exception {
      System.out.printf("%n.. File: %s%n", file.getName());
      String prefix = String.format("{\"@type\":\"%s\",", TypeNames.getInternalName(type));

      Progress progress = new Progress();
      LineIterator iterator = FileUtils.lineIterator(file, "UTF-8");
      try {
        while (iterator.hasNext()) {
          String line = iterator.nextLine();
          if (!line.isEmpty()) {
            progress.step();
            line = prefix + line.substring(1);
            T entity = mapper.readValue(line, type);
            String id = addDomainEntity(type, entity, change);
            indexManager.addEntity(type, id);
          }
        }
      } finally {
        LineIterator.closeQuietly(iterator);
        progress.done();
      }
    }
  }

}
