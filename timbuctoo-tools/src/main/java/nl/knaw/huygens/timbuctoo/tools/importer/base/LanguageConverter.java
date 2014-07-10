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

import java.io.File;
import java.io.PrintWriter;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.base.BaseLanguage;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;
import nl.knaw.huygens.timbuctoo.tools.util.EntityToJsonConverter;
import nl.knaw.huygens.timbuctoo.util.Files;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

/**
 * Converts languages from a file with ISO-639-3 language codes to JSON.
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
public class LanguageConverter extends CSVImporter {

  public static void main(String[] args) throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();

    // Handle commandline arguments
    String directoryName = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/general/";
    File directory = new File(directoryName);
    if (!directory.isDirectory()) {
      System.out.println("## Not a directory: " + directoryName);
      System.exit(-1);
    }
    File languageFile = new File(directory, "iso-639-3.tab");
    if (!languageFile.canRead()) {
      System.out.println("## Can not read file: " + languageFile.getAbsolutePath());
      System.exit(-1);
    }

    File outputDirectory = new File("import/base");
    outputDirectory.mkdirs();
    File outputFile = new File(outputDirectory, "baselanguage.json");
    new LanguageConverter(outputFile).handleFile(languageFile, 0, false);

    System.out.printf("-- Time used: %s%n", stopWatch);
  }

  // ---------------------------------------------------------------------------

  private static final char SEPERATOR_CHAR = '\t';
  private static final char QUOTE_CHAR = '"';
  private static final int LINES_TO_SKIP = 1;

  /** Core languages: 10 West-European, Latin, classic Greek. */
  private final Set<String> core = Sets.newHashSet("dan", "deu", "eng", "fra", "grc", "ita", "lat", "nld", "nor", "por", "spa", "swe");

  private final EntityToJsonConverter jsonConverter;
  private final PrintWriter out;
  private int count;

  public LanguageConverter(File outputFile) {
    super(new PrintWriter(System.err), SEPERATOR_CHAR, QUOTE_CHAR, LINES_TO_SKIP);
    jsonConverter = new EntityToJsonConverter();
    out = Files.createPrintWriter(outputFile);
    count = 0;
  }

  @Override
  protected void handleEndOfFile() {
    System.out.printf("%n-- Number of languages : %s%n", count);
    if (out != null) {
      out.close();
    }
  };

  @Override
  protected void handleLine(String[] items) throws ValidationException {
    BaseLanguage language = new BaseLanguage();

    if (items.length < 7) {
      displayError("Expecting at least 7 items", items);
      return;
    }
    count++;

    String iso_639_3 = items[0];
    if (iso_639_3.length() != 3) {
      displayError("First item must be a 3-letter code", items);
      return;
    }
    language.setCode(iso_639_3);

    if (core.contains(iso_639_3)) {
      language.setCore(true);
    }

    language.setName(items[6]);

    try {
      jsonConverter.appendTo(out, language);
    } catch (Exception e) {
      displayError(e.getMessage(), items);
    }
  }

}
