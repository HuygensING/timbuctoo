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

import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

/**
 * Imports languages from a CSV file.
 * 
 * The file is located at {@code http://www-01.sil.org/iso639-3/iso-639-3.tab}.
 *
 * Each line contains 8 fields, separated by a tab:<pre>
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

  private static final char SEPERATOR_CHAR = '|';
  private static final char QUOTE_CHAR = '"';
  private static final int LINES_TO_SKIP = 4;

  private final StorageManager storageManager;

  public LanguageImporter(StorageManager storageManager) {
    super(new PrintWriter(System.err), SEPERATOR_CHAR, QUOTE_CHAR, LINES_TO_SKIP);
    this.storageManager = storageManager;
    System.out.printf("%n=== Importing documents of type 'Language'%n");
  }

  @Override
  protected void handleLine(String[] items) {
    Language language = new Language();

    if (items.length < 7) {
      displayError("Expecting at least 7 items", items);
      return;
    }

    String iso_639_3 = items[0];
    if (iso_639_3.length() != 3) {
      displayError("First item must be a 3-letter code", items);
      return;
    }
    language.addCode("iso_639_3", iso_639_3);

    String iso_639_1 = items[3];
    if (iso_639_1 != null && iso_639_1.length() == 2) {
      language.addCode("iso_639_1", iso_639_1);
    }

    language.setName(items[6]);

    try {
      storageManager.addSystemEntity(Language.class, language);
    } catch (IOException e) {
      displayError(e.getMessage(), items);
    }
  }

}
