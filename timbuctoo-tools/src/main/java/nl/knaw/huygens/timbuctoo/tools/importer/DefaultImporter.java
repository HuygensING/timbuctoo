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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.storage.DuplicateException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.util.Progress;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Contains functionality needed in most importers.
 */
public abstract class DefaultImporter {

  protected final Repository repository;
  protected final IndexManager indexManager;
  protected final Change change;

  public DefaultImporter(Repository repository, IndexManager indexManager, String vreId) {
    this.repository = Preconditions.checkNotNull(repository);
    this.indexManager = Preconditions.checkNotNull(indexManager);
    change = new Change("importer", vreId);
  }

  /**
   * Closes the resources used by the importer.
   */
  public void close() {
    repository.close();
    indexManager.close();
  }

  // --- Error handling --------------------------------------------------------

  private int errors = 0;
  private String prevMessage = "";

  protected void handleError(String format, Object... args) {
    errors++;
    String message = String.format(format, args);
    if (!message.equals(prevMessage)) {
      System.out.print("## ");
      System.out.print(message);
      System.out.println();
      prevMessage = message;
    }
  }

  protected void displayErrorSummary() {
    if (errors > 0) {
      System.out.printf("%n## Error count = %d%n", errors);
    }
  }

  // --- Import log ------------------------------------------------------------

  private Writer importLog;
  private String source;

  protected void openImportLog(String fileName) throws IOException {
    File file = new File(fileName);
    FileOutputStream fos = new FileOutputStream(file);
    OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
    importLog = new BufferedWriter(out);
  }

  protected void closeImportLog() throws IOException {
    if (importLog != null) {
      importLog.close();
    }
  }

  protected void openSource(String source) {
    this.source = source;
  }

  protected void closeSource() {
    source = null;
  }

  protected void log(String format, Object... args) {
    String text = String.format(format, args);
    if (importLog != null) {
      try {
        if (source != null) {
          importLog.write(String.format("-- %s%n", source));
        }
        importLog.write(text);
        return;
      } catch (IOException e) {
        // ignore
      }
    }
    System.out.println(text);
  }

  // --- Storage ---------------------------------------------------------------

  protected <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity) {
    try {
      repository.addDomainEntity(type, entity, change);
      return entity.getId();
    } catch (Exception e) {
      handleError("Failed to add %s; %s", entity.getDisplayName(), e.getMessage());
      return null;
    }
  }

  protected <T extends DomainEntity> T updateProjectDomainEntity(Class<T> type, T entity) {
    if (TypeRegistry.isPrimitiveDomainEntity(type)) {
      handleError("Unexpected update of primitive domain entity %s", type.getSimpleName());
      return null;
    }
    try {
      repository.updateDomainEntity(type, entity, change);
      return entity;
    } catch (StorageException e) {
      handleError("Failed to update %s; %s", entity.getDisplayName(), e.getMessage());
      return null;
    }
  }

  protected <T extends DomainEntity> void ensureVariation(Class<T> type, String id) {
    try {
      repository.ensureVariation(type, id, change);
    } catch (StorageException e) {
      handleError("Failed to ensure %s variation for id %s", type.getSimpleName(), id);
    }
  }

  // --- Relations -------------------------------------------------------------

  protected void importRelationTypes() throws Exception {
    new RelationTypeImporter(repository).call(RelationTypeImporter.RELATION_TYPE_DEFS);
  }

  protected final Map<String, Reference> relationTypes = Maps.newHashMap();

  protected void setupRelationTypeDefs() {
    for (RelationType type : repository.getSystemEntities(RelationType.class).getAll()) {
      relationTypes.put(type.getRegularName(), new Reference(RelationType.class, type.getId()));
    }
  }

  protected Reference getRelationTypeRef(String name, boolean required) {
    Reference reference = relationTypes.get(name);
    if (reference == null && required) {
      throw new RuntimeException("Missing relation type " + name);
    }
    return reference;
  }

  private int duplicateRelationCount = 0;

  protected int getDuplicateRelationCount() {
    return duplicateRelationCount;
  }

  protected <T extends Relation> String addRelation(Class<T> type, Reference relType, Reference source, Reference target, Change change, String line) {
    T relation = RelationBuilder.newInstance(type) //
        .withRelationTypeRef(relType) //
        .withSourceRef(source) //
        .withTargetRef(target) //
        .build();
    try {
      return repository.addDomainEntity(type, relation, change);
    } catch (DuplicateException e) {
      duplicateRelationCount++;
      log("Duplicate relation: %s%n", line);
    } catch (ValidationException e) {
      log("Invalid relation: %s in %s%n", e.getMessage(), line);
    } catch (StorageException e) {
      log("Failed to store relation: %s%n", line);
    }
    return null;
  }

  // ---------------------------------------------------------------------------

  /**
   * Deletes the non persisted entity's of {@code type} and it's relations from the storage and the index.
   */
  protected void removeNonPersistentEntities(Class<? extends DomainEntity> type) throws StorageException, IndexException {
    List<String> ids = repository.getAllIdsWithoutPID(type);
    repository.deleteNonPersistent(type, ids);
    indexManager.deleteEntities(type, ids);

    List<String> relationIds = repository.getRelationIds(ids);
    repository.deleteNonPersistent(Relation.class, relationIds);
    indexManager.deleteEntities(Relation.class, ids);
  }

  /**
   * Indexes all domain entities with the specified type.
   */
  protected <T extends DomainEntity> void indexEntities(Class<T> type) throws IndexException {
    System.out.println(".. " + type.getSimpleName());
    Progress progress = new Progress();
    StorageIterator<T> iterator = null;
    try {
      iterator = repository.getDomainEntities(type);
      while (iterator.hasNext()) {
        progress.step();
        T entity = iterator.next();
        indexManager.addEntity(type, entity.getId());
      }
      indexManager.commitAll();
    } finally {
      progress.done();
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
    System.out.println(repository.getStatus());
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
  }

  /**
   * Filters a text field by collapsing whitespace and removing leading and trailing whitespace.
   * Returns {@code null} if the remaining text is empty.
   */
  protected String filterField(String text) {
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

  /** Line separator in note fields */
  public static final String NEWLINE = "\n";

  /**
   * Filters a notes text field by collapsing whitespace and removing leading and trailing whitespace.
   * Newlines are  retained
   * Returns {@code null} if the remaining text is empty.
   */
  protected String filterNotesField(String text) {
    if (text == null) {
      return null;
    }
    if (text.contains("\\")) {
      text = text.replaceAll("\\\\r", NEWLINE);
      text = text.replaceAll("\\\\n", NEWLINE);
    }
    text = text.replaceAll("[ \\u00A0]+", " ");
    return StringUtils.stripToNull(text);
  }

}
