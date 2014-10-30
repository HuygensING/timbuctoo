package nl.knaw.huygens.timbuctoo.tools.other;

import static nl.knaw.huygens.timbuctoo.storage.file.LoginCollection.LOGIN_COLLECTION_FILE_NAME;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class LocalLoginCreator {

  public static void main(String[] args) throws Exception {
    Login login = new Login("test", "test", "test", "test", "test", "test");

    Injector injector = Guice.createInjector(new ToolsInjectionModule(new Configuration("config.xml")));

    JsonFileHandler jsonFileHandler = injector.getInstance(JsonFileHandler.class);
    LoginCollection collection = jsonFileHandler.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME);

    collection.add(login);

    jsonFileHandler.saveCollection(collection, LOGIN_COLLECTION_FILE_NAME);
  }
}
