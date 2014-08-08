package nl.knaw.huygens.timbuctoo.tools.importer.neww;

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
import java.util.Map;

import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

import com.google.common.collect.Maps;

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

  public RelationTypeConcordance(File file) throws Exception {
    super(new PrintWriter(System.err), ';', '"');
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
