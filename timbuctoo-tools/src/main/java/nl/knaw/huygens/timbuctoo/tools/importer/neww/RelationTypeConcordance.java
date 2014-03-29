package nl.knaw.huygens.timbuctoo.tools.importer.neww;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.google.common.collect.Maps;

import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

public class RelationTypeConcordance extends CSVImporter {

  public static class Mapping {
    public Mapping(String oldName, String newName, boolean inverse) {
      this.oldName = oldName;
      this.newName = newName;
      this.inverse = inverse;
    }

    public String oldName;
    public String newName;
    public boolean inverse;
  }

  private final Map<String, Mapping> map = Maps.newHashMap();

  public RelationTypeConcordance(File file) throws IOException, ValidationException {
    super(new PrintWriter(System.err), ';', '"', 4);
    if (file != null) {
      handleFile(file, 6, false);
    }
  }

  @Override
  protected void handleLine(String[] items) {
    String key = createKey(items[0], items[1], items[2]);
    boolean inverse = isInverse(items[3]);
    String oldName = items[0];
    String newName = inverse ? items[5] : items[4];
    Mapping mapping = new Mapping(oldName, newName, inverse);

    if (map.put(key, mapping) != null) {
      throw new RuntimeException("Duplicate entry for key " + key);
    }
  }

  @Override
  protected void handleEndOfFile() {
    System.out.printf("Concordance size : %d%n", map.size());
  };

  private String createKey(String name, String leftType, String rightType) {
    return String.format("%s#%s#%s", name, leftType, rightType);
  }
  
  private boolean isInverse(String text) {
    return text.equalsIgnoreCase("inverse");
  }

  public Mapping lookup(String name, String leftType, String rightType) {
    String key = createKey(name, leftType, rightType);
    return map.get(key);
  }

}
