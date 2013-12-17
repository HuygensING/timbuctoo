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

import java.io.IOException;
import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

/**
 * Imports languages from a CSV file.
 * 
 * The import file is obtained directly from the internet at
 * {@code http://www.loc.gov/standards/iso639-2/ISO-639-2_utf-8.txt}
 *
 * Each line contains 5 fields, separated by a '|' character:
 * - bibliographic code, 3 letters, always present
 * - terminology code, 3 letters, optional
 * - alpha-2 code, 2 letters, optional
 * - English name
 * - French name
 * 
 * N.B. Removed line
 * qaa-qtz|||Reserved for local use|réservée à l'usage local
 */
public class LanguageImporter extends CSVImporter {

  private final Change change;
  private StorageManager storageManager;

  public LanguageImporter(StorageManager storageManager) {
    super(new PrintWriter(System.err), '|', '"', 0);
    change = new Change("importer", "timbuctoo");
    this.storageManager = storageManager;
    System.out.printf("%n=== Importing documents of type 'Language'%n");
  }

  @Override
  protected void handleLine(String[] items) {
    Language language = new Language();

    if (items[0].length() != 3) {
      displayError("first item must be 3-letter code", items);
      return;
    }
    language.addCode("iso_639_2", items[0]);

    if (items[1].length() != 0) {
      if (items[1].length() != 3) {
        displayError("second item must be 3-letter code", items);
        return;
      }
      language.addCode("iso_639_2t", items[1]);
    }

    if (items[2].length() != 0) {
      if (items[2].length() != 2) {
        displayError("third item must be 2-letter code", items);
        return;
      }
      language.addCode("iso_639_1", items[2]);
    }

    language.setName(items[3]);

    try {
      storageManager.addDomainEntity(Language.class, language, change);
    } catch (IOException e) {
      displayError(e.getMessage(), items);
    }
  }

}
