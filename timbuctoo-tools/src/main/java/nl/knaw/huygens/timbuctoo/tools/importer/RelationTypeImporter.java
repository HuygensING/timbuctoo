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
import java.io.InputStream;
import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

/**
 * Imports all relation types.
 * Relies on the storage manager for validation.
 */
public class RelationTypeImporter extends CSVImporter {

  private final StorageManager storageManager;

  public RelationTypeImporter(StorageManager manager) {
    super(new PrintWriter(System.err), ';', '"', 4);
    this.storageManager = manager;
  }

  /**
   * Reads {@code RelationType} definitions from the specified file which must
   * be present on the classpath.
   */
  public void importRelationTypes(String fileName) throws IOException, ValidationException {
    InputStream stream = StorageManager.class.getClassLoader().getResourceAsStream(fileName);
    handleFile(stream, 6, false);
  }

  @Override
  protected void handleLine(String[] items) throws IOException, ValidationException {
    RelationType entity = new RelationType();
    entity.setRegularName(items[0]);
    entity.setInverseName(items[1]);
    entity.setSourceTypeName(items[2].toLowerCase());
    entity.setTargetTypeName(items[3].toLowerCase());
    entity.setReflexive(Boolean.parseBoolean(items[4]));
    entity.setSymmetric(Boolean.parseBoolean(items[5]));
    try {
      if (storageManager.findEntity(RelationType.class, "regularName", entity.getRegularName()) == null) {
        storageManager.addSystemEntity(RelationType.class, entity);
      }
    } catch (StorageException e) {
      throw new IOException(e);
    }
  }

}
