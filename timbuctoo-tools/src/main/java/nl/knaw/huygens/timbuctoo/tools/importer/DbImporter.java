package nl.knaw.huygens.timbuctoo.tools.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.mongojack.internal.MongoJacksonMapperModule;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DbImporter {

  private final Configuration conf;
  private final TypeRegistry typeRegistry;
  private final StorageManager storageManager;

  public DbImporter(Configuration conf, TypeRegistry registry, StorageManager manager) {
    this.conf = conf;
    typeRegistry = registry;
    storageManager = manager;
  }

  public <T extends Entity> void bulkImport(Class<T> type, boolean setChange, String vreId, String vreName) {
    ObjectMapper mapper = new ObjectMapper();
    MongoJacksonMapperModule.configure(mapper);
    try {
      System.out.printf("=== Importing documents of type '%s'%n", type.getSimpleName());
      String collectionName = typeRegistry.getINameForType(type);
      BufferedReader input = createReader(collectionName);
      int count = 0;
      String line = null;
      while ((line = input.readLine()) != null) {
        count++;
        T item = mapper.readValue(line, type);
        if (setChange) {
          Change change = new Change(new Date().getTime(), "database-id", "Database import", vreId, vreName);
          if (item.getLastChange() == null) {
            item.setLastChange(change);
          }
          if (item.getCreation() == null) {
            item.setCreation(change);
          }
        }
        if (item.getRev() == 0) {
          item.setRev(1);
        }
        storageManager.addEntity(type, item);
        System.out.print((count % 100 == 99) ? ".\n" : ".");
      }
      System.out.print("\nImported " + count + " items.\n\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private <T> BufferedReader createReader(String collectionName) throws UnsupportedEncodingException, FileNotFoundException {
    File file = new File(conf.getSetting("paths.json", "") + '/' + collectionName + ".json");
    // Because file reading shouldn't be easy:
    String charset = conf.getSetting("importencoding", "UTF8");
    return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
  }

}
