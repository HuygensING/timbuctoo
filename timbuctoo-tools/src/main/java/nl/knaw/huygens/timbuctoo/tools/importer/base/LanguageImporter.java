package nl.knaw.huygens.timbuctoo.tools.importer.base;

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

import java.io.PrintWriter;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.base.BaseLanguage;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

import com.google.common.collect.Sets;
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

  private static final char SEPERATOR_CHAR = '\t';
  private static final char QUOTE_CHAR = '"';
  private static final int LINES_TO_SKIP = 1;

  private final Repository repository;
  private final Change change;

  /** Core languages: 10 West-European, Latin, classic Greek. */
  private final Set<String> core = Sets.newHashSet("dan", "deu", "eng", "fra", "grc", "ita", "lat", "nld", "nor", "por", "spa", "swe");

  private int totalCount;
  private int coreCount;

  public LanguageImporter(Repository repository, Change change) {
    super(new PrintWriter(System.err), SEPERATOR_CHAR, QUOTE_CHAR, LINES_TO_SKIP);
    this.repository = repository;
    this.change = change;
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
  protected void handleLine(String[] items) throws ValidationException {
    BaseLanguage language = new BaseLanguage();

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

    if (core.contains(iso_639_3)) {
      coreCount++;
      language.setCore(true);
      System.out.printf("%s - %s%n", iso_639_3, items[6]);
    }

    language.setName(items[6]);

    try {
      repository.addDomainEntity(BaseLanguage.class, language, change);
    } catch (MongoException.DuplicateKey e) {
      displayError("Duplicate key", items);
    } catch (StorageException e) {
      displayError(e.getMessage(), items);
    }
  }

}
