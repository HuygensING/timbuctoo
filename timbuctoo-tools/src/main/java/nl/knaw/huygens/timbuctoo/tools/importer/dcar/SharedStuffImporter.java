package nl.knaw.huygens.timbuctoo.tools.importer.dcar;

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

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.LanguageImporter;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class SharedStuffImporter extends DutchCaribbeanDefaultImporter {

  public static void main(String[] args) throws Exception {

    // Handle commandline arguments
    String importDirName = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/general/iso-639-3-language-codes.txt";
    String configFileName = (args.length > 1) ? args[1] : "config.xml";

    Configuration config = new Configuration(configFileName);
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    StorageManager storageManager = null;

    try {
      long start = System.currentTimeMillis();

      storageManager = injector.getInstance(StorageManager.class);

      TypeRegistry registry = injector.getInstance(TypeRegistry.class);
      new SharedStuffImporter(registry, storageManager, importDirName).importAll();

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

    } catch (Exception e) {
      // for debugging
      e.printStackTrace();
    } finally {
      // Close resources
      if (storageManager != null) {
        storageManager.close();
      }
      // If the application is not explicitly closed a finalizer thread of Guice keeps running.
      System.exit(0);
    }
  }

  // -------------------------------------------------------------------

  private final File inputDir;

  public SharedStuffImporter(TypeRegistry registry, StorageManager storageManager, String inputDirName) {
    super(registry, storageManager, null, null);
    inputDir = new File(inputDirName);
    System.out.printf("%n.. Importing from %s%n", inputDir.getAbsolutePath());
  }

  public void importAll() throws Exception {

    printBoxedText("Languages");

    LanguageImporter importer = new LanguageImporter(storageManager);
    importer.handleFile(inputDir, 0, false);

    //    System.out.printf("%n.. Setup relation types%n");
    //    // FIXME system entities shouldn't have been removed!
    //    setup(relationManager);

    displayErrorSummary();
  }

  private void printBoxedText(String text) {
    String line = Strings.repeat("-", text.length() + 8);
    System.out.println();
    System.out.println(line);
    System.out.print("--  ");
    System.out.print(text);
    System.out.println("  --");
    System.out.println(line);
    System.out.println();
  }

}
