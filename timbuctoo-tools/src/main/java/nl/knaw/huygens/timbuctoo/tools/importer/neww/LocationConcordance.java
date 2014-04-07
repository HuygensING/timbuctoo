package nl.knaw.huygens.timbuctoo.tools.importer.neww;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.google.common.collect.Maps;

import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

/**
 * Normalizes names of locations.
 */
public class LocationConcordance extends CSVImporter {

  private final Map<String, String> map = Maps.newHashMap();

  public LocationConcordance(File file) throws IOException, ValidationException {
    super(new PrintWriter(System.err));
    if (file != null) {
      handleFile(file, 2, false);
    }
  }

  @Override
  protected void handleLine(String[] items) {
    if (map.containsKey(items[0])) {
      throw new RuntimeException("Duplicate entry for key " + items[0]);
    }
    map.put(items[0], items[1]);
  }

  @Override
  protected void handleEndOfFile() {
    System.out.printf("Location concordance size : %d%n", map.size());
  };

  public String lookup(String text) {
    return map.get(text);
  }

}
