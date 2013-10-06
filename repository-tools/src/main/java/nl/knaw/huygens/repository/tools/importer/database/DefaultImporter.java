package nl.knaw.huygens.repository.tools.importer.database;

import java.io.IOException;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.model.EntityRef;
import nl.knaw.huygens.repository.storage.StorageManager;
import nl.knaw.huygens.repository.tools.ToolBase;

public abstract class DefaultImporter extends ToolBase {

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

  protected <T extends Entity> T getEntity(Class<T> type, String id) {
    return storageManager.getEntity(type, id);
  }

  protected <T extends Entity> String addEntity(Class<T> type, T entity, boolean isComplete) {
    try {
      storageManager.addEntityWithoutPersisting(type, entity, isComplete);
      return entity.getId();
    } catch (IOException e) {
      handleError("Failed to add %s; %s", entity.getDisplayName(), e.getMessage());
      return null;
    }
  }

  protected <T extends Entity> T modEntity(Class<T> type, T entity) {
    try {
      storageManager.modifyEntityWithoutPersisting(type, entity);
      return entity;
    } catch (IOException e) {
      handleError("Failed to modify %s; %s", entity.getDisplayName(), e.getMessage());
      return null;
    }
  }

  // -------------------------------------------------------------------

  protected <T extends Entity> EntityRef newEntityRef(Class<T> type, T entity) {
    String itype = docTypeRegistry.getINameForType(type);
    String xtype = docTypeRegistry.getXNameForType(type);
    return new EntityRef(itype, xtype, entity.getId(), entity.getDisplayName());
  }

  protected <T extends Entity> EntityRef newEntityRef(Class<T> type, String id) {
    String itype = docTypeRegistry.getINameForType(type);
    String xtype = docTypeRegistry.getXNameForType(type);
    return new EntityRef(itype, xtype, id, null);
  }

}
