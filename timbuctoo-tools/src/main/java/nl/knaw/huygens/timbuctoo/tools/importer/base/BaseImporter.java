package nl.knaw.huygens.timbuctoo.tools.importer.base;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultImporter;

import com.google.inject.Guice;
import com.google.inject.Injector;

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

/**
 * Importer for base domain entities, such as language.
 */
public class BaseImporter extends DefaultImporter {

  private static final String USER_ID = "importer";
  private static final String VRE_ID = "base";

  public static void main(String[] args) throws Exception {

    String fileName = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/general/iso-639-3.tab";

    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    StorageManager storageManager = null;
    IndexManager indexManager = null;

    try {
      TypeRegistry registry = injector.getInstance(TypeRegistry.class);
      storageManager = injector.getInstance(StorageManager.class);
      indexManager = injector.getInstance(IndexManager.class);

      // Get rid of existing stuff
      BaseImporter i = new BaseImporter(registry, storageManager, indexManager);
      i.removeNonPersistentEntities(Language.class);

      LanguageImporter importer = new LanguageImporter(storageManager, USER_ID, VRE_ID);
      importer.handleFile(fileName, 0, false);

    } catch (Exception e) {
      // for debugging
      e.printStackTrace();
    } finally {
      // Close resources
      if (indexManager != null) {
        indexManager.close();
      }
      if (storageManager != null) {
        storageManager.close();
      }
      // If the application is not explicitly closed a finalizer thread of Guice keeps running.
      System.exit(0);
    }
  }

  // ---------------------------------------------------------------------------

  public BaseImporter(TypeRegistry typeRegistry, StorageManager storageManager, IndexManager indexManager) {
    super(typeRegistry, storageManager, indexManager);
  }

}
