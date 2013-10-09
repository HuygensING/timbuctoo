package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.io.IOException;
import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.util.CSVImporter;

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

  private StorageManager storageManager;

  public LanguageImporter(StorageManager storageManager) {
    super(new PrintWriter(System.err), '|', '"', 0);
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
      storageManager.addEntity(Language.class, language, false);
    } catch (IOException e) {
      displayError(e.getMessage(), items);
    }
  }

}
