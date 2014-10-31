package nl.knaw.huygens.timbuctoo.tools.importer;

import static nl.knaw.huygens.timbuctoo.storage.file.LoginCollection.LOGIN_COLLECTION_FILE_NAME;

import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.security.PasswordEncrypter;
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
    PasswordEncrypter passwordEncrypter = injector.getInstance(PasswordEncrypter.class);

    LocalLoginImporter localLoginCreator = new LocalLoginImporter(jsonFileHandler, passwordEncrypter);

    if (args.length > 0) {
      localLoginCreator.handleFile(args[0], 7, false);
    } else {
      System.out.println("Please provide an input file as first argument.");
    }
  }

  private LoginCollection collection;
  private JsonFileHandler jsonFileHandler;
  private PasswordEncrypter passwordEncrypter;

  public LocalLoginImporter(JsonFileHandler jsonFileHandler, PasswordEncrypter passwordEncrypter) throws Exception {
    super(new PrintWriter(System.out));
    this.jsonFileHandler = jsonFileHandler;
    this.passwordEncrypter = passwordEncrypter;
    collection = jsonFileHandler.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME);

  }

  private void addLogin(Login login) throws Exception {
    LOG.info("Add login for \"{}\"", login.getCommonName());
    collection.add(login);
    jsonFileHandler.saveCollection(collection, LOGIN_COLLECTION_FILE_NAME);
  }

  @Override
  protected void handleLine(String[] items) throws Exception {
    String userPid = items[0];
    String userName = items[1];
    String password = items[2];
    String givenName = items[3];
    String surname = items[4];
    String emailAddress = items[5];
    String organization = items[6];

    byte[] salt = passwordEncrypter.createSalt();
    String encryptedPassword = passwordEncrypter.encryptPassword(password, salt);

    Login login = new Login(userPid, userName, encryptedPassword, givenName, surname, emailAddress, organization, salt);
    addLogin(login);
  }

}
