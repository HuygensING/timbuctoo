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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.google.common.collect.Maps;

import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;
import nl.knaw.huygens.timbuctoo.validation.ValidationException;

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
