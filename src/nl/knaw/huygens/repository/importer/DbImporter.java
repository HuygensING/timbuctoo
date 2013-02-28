package nl.knaw.huygens.repository.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import org.mongojack.internal.MongoJacksonMapperModule;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.util.Configuration;

public class DbImporter {
  private final Configuration conf;
  private final StorageManager storage;
  public DbImporter(Configuration conf, StorageManager storage) {
    this.conf = conf;
    this.storage = storage;
  }
  public <T extends Document> void bulkImport(Class<T> entityClass, boolean setChange, String vreId, String vreName)  {
    ObjectMapper mapper = new ObjectMapper();
    MongoJacksonMapperModule.configure(mapper);
    try {
      System.out.println("Importing " + entityClass.getSimpleName());
      BufferedReader input = createReader(entityClass);
      List<T> collection = Lists.newArrayList();
      String line = null;
      while ((line = input.readLine()) != null) {
        T item = mapper.readValue(line, entityClass);
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
      storage.getStorage().addItems(collection, entityClass);
      System.out.print("\nImported " + collection.size() + " items.\n\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  protected <T> BufferedReader createReader(Class<T> entityClass) throws UnsupportedEncodingException, FileNotFoundException {
    File f = new File(conf.getSetting("paths.json", "") + '/' + entityClass.getSimpleName().toLowerCase() + ".json");
    // Because file reading shouldn't be easy:
    String charsetForReading = conf.getSetting("importencoding", "UTF8");
    BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(f), charsetForReading));
    return input;
  }
}
