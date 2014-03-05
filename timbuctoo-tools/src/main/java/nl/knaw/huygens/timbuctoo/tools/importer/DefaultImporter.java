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
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Strings;

/**
 * Contains functionality needed in each importer.
 */
public abstract class DefaultImporter {

  protected final TypeRegistry typeRegistry;
  protected final StorageManager storageManager;
  protected final IndexManager indexManager;

  public DefaultImporter(TypeRegistry typeRegistry, StorageManager storageManager, IndexManager indexManager) {
    this.typeRegistry = typeRegistry;
    this.storageManager = storageManager;
    this.indexManager = indexManager;
  }

  // --- Error handling --------------------------------------------------------

  private int errors = 0;
  private String prevMessage = "";

  protected void handleError(String format, Object... args) {
    errors++;
    String message = String.format(format, args);
    if (!message.equals(prevMessage)) {
      System.out.print("## ");
      System.out.printf(message);
      System.out.println();
      prevMessage = message;
    }
  }

  protected void displayErrorSummary() {
    if (errors > 0) {
      System.out.printf("%n## Error count = %d%n", errors);
    }
  }

  // --- Relations -------------------------------------------------------------

  /** File with {@code RelationType} definitions; must be present on classpath. */
  private static final String RELATION_TYPE_DEFS = "relationtype-defs.txt";

  protected void setup(RelationManager relationManager) {
    if (relationManager != null) {
      new RelationTypeImporter(relationManager, this.typeRegistry).importRelationTypes(RELATION_TYPE_DEFS);
    }
  }

  // ---------------------------------------------------------------------------

  /**
   * Deletes the non persisted entity's of {@code type} and it's relations from the storage and the index.
   */
  protected void removeNonPersistentEntities(Class<? extends DomainEntity> type) throws IOException, IndexException {
    Class<? extends DomainEntity> baseType = TypeRegistry.toDomainEntity(typeRegistry.getBaseClass(type));

    List<String> ids = storageManager.getAllIdsWithoutPIDOfType(type);
    storageManager.deleteNonPersistent(type, ids);
    indexManager.deleteEntities(baseType, ids);

    List<String> relationIds = storageManager.getRelationIds(ids);
    storageManager.deleteNonPersistent(Relation.class, relationIds);
    indexManager.deleteEntities(Relation.class, ids);
  }

  /**
   * Indexes all domain entities with the specified type.
   */
  protected <T extends DomainEntity> void indexEntities(Class<T> type) throws IndexException {
    System.out.println(".. " + type.getSimpleName());
    StorageIterator<T> iterator = null;
    try {
      iterator = storageManager.getAll(type);
      while (iterator.hasNext()) {
        T entity = iterator.next();
        indexManager.addEntity(type, entity.getId());
      }
    } finally {
      if (iterator != null) {
        iterator.close();
      }
    }
  }

  /**
   * Displays the status of the Mongo database and the Solr indexes.
   */
  protected void displayStatus() throws IndexException {
    // Make sure the Solr indexes are up-to-date
    indexManager.commitAll();

    System.out.println();
    System.out.println(storageManager.getStatus());
    System.out.println(indexManager.getStatus());
  }

  /**
   * Displays a text in a formatted box.
   */
  protected void printBoxedText(String text) {
    String line = Strings.repeat("-", text.length() + 8);
    System.out.println();
    System.out.println(line);
    System.out.print("--  ");
    System.out.print(text);
    System.out.println("  --");
    System.out.println(line);
    System.out.println();
  }

  /**
   * Filters a text field by collapsing whitespace and removing leading and trailing whitespace.
   * Returns {@code null} if the remaining text is empty.
   */
  protected String filterTextField(String text) {
    if (text == null) {
      return null;
    }
    if (text.contains("\\")) {
      text = text.replaceAll("\\\\r", " ");
      text = text.replaceAll("\\\\n", " ");
    }
    text = text.replaceAll("[\\s\\u00A0]+", " ");
    return StringUtils.stripToNull(text);
  }

}
