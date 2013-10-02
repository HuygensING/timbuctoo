package nl.knaw.huygens.repository.tools.importer.database;

import java.io.IOException;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.model.EntityRef;
import nl.knaw.huygens.repository.storage.StorageManager;

public abstract class DefaultImporter {

  protected final DocTypeRegistry docTypeRegistry;
  private final StorageManager storageManager;

  private String prevMessage;
  private int errors;

  public DefaultImporter(DocTypeRegistry registry, StorageManager storageManager) {
    this.docTypeRegistry = registry;
    this.storageManager = storageManager;
    prevMessage = "";
    errors = 0;
  }

  // --- error handling ------------------------------------------------

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

  // --- storage -------------------------------------------------------

  protected <T extends Entity> T getDocument(Class<T> type, String id) {
    return storageManager.getEntity(type, id);
  }

  protected <T extends Entity> String addDocument(Class<T> type, T document, boolean isComplete) {
    try {
      storageManager.addEntityWithoutPersisting(type, document, isComplete);
      return document.getId();
    } catch (IOException e) {
      handleError("Failed to add %s; %s", document.getDisplayName(), e.getMessage());
      return null;
    }
  }

  protected <T extends Entity> T modDocument(Class<T> type, T document) {
    try {
      storageManager.modifyEntityWithoutPersisting(type, document);
      return document;
    } catch (IOException e) {
      handleError("Failed to modify %s; %s", document.getDisplayName(), e.getMessage());
      return null;
    }
  }

  // -------------------------------------------------------------------

  protected <T extends Entity> EntityRef newDocumentRef(Class<T> type, T document) {
    String itype = docTypeRegistry.getINameForType(type);
    String xtype = docTypeRegistry.getXNameForType(type);
    return new EntityRef(itype, xtype, document.getId(), document.getDisplayName());
  }

  protected <T extends Entity> EntityRef newDocumentRef(Class<T> type, String id) {
    String itype = docTypeRegistry.getINameForType(type);
    String xtype = docTypeRegistry.getXNameForType(type);
    return new EntityRef(itype, xtype, id, null);
  }

}
