package nl.knaw.huygens.timbuctoo.tools.importer;

import static nl.knaw.huygens.timbuctoo.storage.file.LoginCollection.LOGIN_COLLECTION_FILE_NAME;

import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class LocalLoginImporter extends CSVImporter {
  private static final Logger LOG = LoggerFactory.getLogger("LocalLoginImporter");

  public static void main(String[] args) throws Exception {
    Injector injector = ToolsInjectionModule.createInjector();

    JsonFileHandler jsonFileHandler = injector.getInstance(JsonFileHandler.class);

    LocalLoginImporter localLoginCreator = new LocalLoginImporter(jsonFileHandler);

    if (args.length > 0) {
      localLoginCreator.handleFile(args[0], 6, false);
    } else {
      System.out.println("Please provide an input file as first argument.");
    }
  }

  private LoginCollection collection;
  private JsonFileHandler jsonFileHandler;

  public LocalLoginImporter(JsonFileHandler jsonFileHandler) throws Exception {
    super(new PrintWriter(System.out));
    this.jsonFileHandler = jsonFileHandler;
    collection = jsonFileHandler.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME);

  }

  private void addLogin(Login login) throws Exception {
    LOG.info("Add login for \"{}\"", login.getCommonName());
    collection.add(login);
    jsonFileHandler.saveCollection(collection, LOGIN_COLLECTION_FILE_NAME);
  }

  @Override
  protected void handleLine(String[] items) throws Exception {
    Login login = new Login(items[0], items[1], items[2], items[3], items[4], items[5]);
    addLogin(login);
  }
}
