package nl.knaw.huygens.repository.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;

import org.mongojack.internal.MongoJacksonMapperModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class DbImporter {

  private final Configuration conf;
  private final StorageManager storage;

  public DbImporter(Configuration conf, StorageManager storage) {
    this.conf = conf;
    this.storage = storage;
  }

  public <T extends Document> void bulkImport(Class<T> type, boolean setChange, String vreId, String vreName) {
    ObjectMapper mapper = new ObjectMapper();
    MongoJacksonMapperModule.configure(mapper);
    try {
      System.out.printf("=== Importing documents of type '%s'%n", type.getSimpleName());
      String collectionName = DocTypeRegistry.getCollectionName(type);
      BufferedReader input = createReader(collectionName);
      List<T> collection = Lists.newArrayList();
      String line = null;
      while ((line = input.readLine()) != null) {
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
        collection.add(item);
        System.out.print((collection.size() % 100 == 99) ? ".\n" : ".");
      }
      storage.getStorage().addItems(type, collection);
      System.out.print("\nImported " + collection.size() + " items.\n\n");
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
