package nl.knaw.huygens.timbuctoo.tools.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class DatabaseSetupper {

  private static final String FILE_FILTER = ".tab";

  private final Configuration config;
  private final DocTypeRegistry typeRegistry;
  private final StorageManager storageManager;
  private final DbImporter importer;
  private File sourceDir;
  private File jsonDir;
  private BufferedWriter errors;
  private String vreName;
  private String vreId;

  @Inject
  public DatabaseSetupper(Configuration config, DocTypeRegistry registry, StorageManager storageManager, DbImporter importer) {
    this.config = config;
    this.typeRegistry = registry;
    this.storageManager = storageManager;
    this.importer = importer;
    initialize();
  }

  public void setVREId(String vreId, String vreName) {
    this.vreId = vreId;
    this.vreName = vreName;
  }

  public int run() throws IOException {
    System.out.println("Emptying the database...");
    storageManager.clear();
    System.out.println("Emptied the database.");

    if (config.getBooleanSetting("dataNeedsCleaning", false)) {
      System.out.println("Cleaning input data...");
      importCleaner();
    }
    for (String model : config.getSettings("doctypes")) {
      Class<? extends Entity> cls = typeRegistry.getTypeForIName(model);
      importer.bulkImport(cls, true, vreId, vreName);
    }
    createAdminUser();
    System.out.println("Done.");
    return 0;
  }

  private void createAdminUser() throws IOException {
    User admin = new User();
    admin.setId(null); // Will be filled in by the storage implementation.
    admin.email = "admin@example.com";
    admin.groups = Lists.newArrayList("administrator");
    admin.firstName = "Joe";
    admin.lastName = "Administrator";
    storageManager.addEntity(User.class, admin);
    System.out.println("Added default user.");
  }

  protected void initialize() {
    try {
      jsonDir = new File(config.getSetting("paths.json", ""));
      jsonDir.delete();
      jsonDir.mkdir();

      File errorReport = new File("import-report.txt");
      errors = new BufferedWriter(new FileWriter(errorReport));

      sourceDir = new File(config.getSetting("paths.source", ""));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void importCleaner() {
    String charsetToUse = config.getSetting("importencoding", "UTF8");
    try {
      System.out.println("Starting...");
      FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(FILE_FILTER);
        }
      };

      ObjectMapper mapper = new ObjectMapper();

      for (File importFile : sourceDir.listFiles(filter)) {
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(importFile), charsetToUse));
        File outputFile = new File(jsonDir.getCanonicalPath() + '/' + importFile.getName().replace(FILE_FILTER, "") + ".json");
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), charsetToUse));
        String line = null;

        errors.write("\n\n" + importFile.getName().replace(FILE_FILTER, "") + "\n");
        errors.write("++++++++++++++++++++++++++++++++++++++++++++\n\n");
        boolean noErrors = true;
        int counter = 0;
        while ((line = input.readLine()) != null) {
          counter++;
          line = line.replace("\"{", "{");
          line = line.replace("}\"", "}");
          line = line.replace("\"\"", "\"");
          line = line.replace("\"###null###\"", "null");
          try {
            Map<?, ?> map = mapper.readValue(line, Map.class);
            output.write(line + "\n");
            System.out.print((counter % 10 == 9) ? map.get("^type") + " " + map.get("_id") + " |\n" : map.get("^type") + " " + map.get("_id") + " | ");
          } catch (Exception e) {
            noErrors = false;
            System.out.print(e.getMessage() + " : " + line + "\n");
            errors.write(e.getMessage() + " : " + line + "\n");
          } finally {
            input.close();
            output.close();
            errors.flush();
          }
        }

        if (noErrors) {
          errors.write("No Errors!");
        }

        System.out.println("\nPrepared and written " + importFile.getName() + "\n");
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

}
