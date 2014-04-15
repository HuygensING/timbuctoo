package nl.knaw.huygens.timbuctoo.tools.importer.ebnm;

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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultImporter;
import nl.knaw.huygens.timbuctoo.tools.importer.RelationTypeImporter;

public abstract class EBNMDefaultImporter extends DefaultImporter {

  /**
   * File with {@code RelationType} definitions; must be present on classpath.
   */
  private static final String RELATION_TYPE_DEFS = "relationtype-defs-codl.txt";

  public EBNMDefaultImporter(TypeRegistry registry, StorageManager storageManager, IndexManager indexManager) {
    super(registry, storageManager, indexManager);
    setup(storageManager);
  }

  protected void setup(StorageManager storageManager) {
    try {
      new RelationTypeImporter(typeRegistry, storageManager).importRelationTypes(RELATION_TYPE_DEFS);
    } catch (ValidationException e) {
      e.printStackTrace();
    }
  }

}
