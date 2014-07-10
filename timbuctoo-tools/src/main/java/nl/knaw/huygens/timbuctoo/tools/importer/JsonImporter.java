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
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.base.BaseLanguage;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

// TODO make more generic
// TODO including date in directory name
public class JsonImporter extends CSVImporter {

  private static final String CONTROL_FILE_NAME = "import.txt";

  public static void main(String[] args) throws Exception {
    // String directoryName = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/import/base/";
    String directoryName = (args.length > 0) ? args[0] : "import/base/";
    File directory = new File(directoryName);

    Injector injector = ToolsInjectionModule.createInjector();
    Repository repository = injector.getInstance(Repository.class);
    IndexManager indexManager = injector.getInstance(IndexManager.class);

    try {
      File file = new File(directory, CONTROL_FILE_NAME);
      JsonImporter importer = new JsonImporter(repository, indexManager, directory);
      importer.handleFile(file, 2, false);
    } finally {
      repository.close();
      indexManager.close();
    }
  }

  // ---------------------------------------------------------------------------

  private final File directory;
  private final Handler handler;

  public JsonImporter(Repository repository, IndexManager indexManager, File directory) {
    super(null);
    this.directory = directory;
    Change change = new Change("importer", "base");
    handler = new Handler(repository, indexManager, change);
  }

  @Override
  protected void handleLine(String[] items) throws Exception {
    File file = new File(directory, items[0]);
    handler.handleFile(file, items[1]);
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

    public void handleFile(File file, String iname) throws Exception {
      Class<? extends DomainEntity> type = BaseLanguage.class;
      System.out.printf("%n-- Importing %s entities from file %s%n", type.getSimpleName(), file.getName());

      int count = 0;
      LineIterator iterator = FileUtils.lineIterator(file, "UTF-8");
      try {
        while (iterator.hasNext()) {
          String line = iterator.nextLine();
          if (!line.isEmpty()) {
            count++;
            line = line.replace("{", "{\"@type\":\"baselanguage\",");
            BaseLanguage entity = mapper.readValue(line, BaseLanguage.class);
            addDomainEntity(BaseLanguage.class, entity, change);
          }
        }
      } finally {
        System.out.printf("Number of entities: %d%n", count);
        LineIterator.closeQuietly(iterator);
      }
    }
  }

}
