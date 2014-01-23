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

import java.io.IOException;
import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.MongoException;

/**
 * Imports languages from a file with ISO-639-3 language codes.
 * Location: {@code http://www-01.sil.org/iso639-3/iso-639-3.tab}.
 *
 * Each line contains 8 fields, separated by tabs:<pre>
 * - iso639-3 code, 3 letters, always present
 * - iso639-2b bibliographic code, 3 letters (deprecated)
 * - iso639-2t terminology code, 3 letters
 * - iso639-1 code, 2 letters
 * - scope, 1 letter
 * - language type, 1 letter
 * - English name
 * - comment
 * </pre>
 */
public class LanguageImporter extends CSVImporter {

  public static void main(String[] args) throws Exception {
    String fileName = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/general/iso-639-3.tab";

    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));
    StorageManager storageManager = null;

    try {
      storageManager = injector.getInstance(StorageManager.class);
      int count = storageManager.deleteSystemEntities(Language.class);
      System.out.printf("%n-- Removed %d languages from store%n", count);

      LanguageImporter importer = new LanguageImporter(storageManager);
      importer.handleFile(fileName, 0, false);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (storageManager != null) {
        storageManager.close();
      }
      System.exit(0);
    }
  }

  // ---------------------------------------------------------------------------

  private static final char SEPERATOR_CHAR = '\t';
  private static final char QUOTE_CHAR = '"';
  private static final int LINES_TO_SKIP = 1;

  private final StorageManager storageManager;

  private int totalCount;
  private int coreCount;

  public LanguageImporter(StorageManager storageManager) {
    super(new PrintWriter(System.err), SEPERATOR_CHAR, QUOTE_CHAR, LINES_TO_SKIP);
    this.storageManager = storageManager;
  }

  @Override
  protected void initialize() {
    totalCount = 0;
    coreCount = 0;
  }

  @Override
  protected void handleEndOfFile() {
    System.out.printf("%n-- Total number of languages : %5s%n", totalCount);
    System.out.printf("-- Number of core languages  : %5s%n", coreCount);
  };

  @Override
  protected void handleLine(String[] items) {
    Language language = new Language();

    if (items.length < 7) {
      displayError("Expecting at least 7 items", items);
      return;
    }
    totalCount++;

    String iso_639_3 = items[0];
    if (iso_639_3.length() != 3) {
      displayError("First item must be a 3-letter code", items);
      return;
    }
    language.setCode(iso_639_3);
    language.addCode("iso_639_3", iso_639_3);

    String iso_639_1 = items[3];
    if (iso_639_1 != null && iso_639_1.length() == 2) {
      coreCount++;
      System.out.printf("%s [%s] - %s%n", iso_639_3, iso_639_1, items[6]);
      language.addCode("iso_639_1", iso_639_1);
    }

    language.setName(items[6]);

    try {
      storageManager.addSystemEntity(Language.class, language);
    } catch (MongoException.DuplicateKey e) {
      displayError("Duplicate key", items);
    } catch (IOException e) {
      displayError(e.getMessage(), items);
    }
  }

}
