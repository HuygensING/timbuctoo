package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.storage.file.LoginCollection.LOGIN_COLLECTION_FILE_NAME;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LocalAuthenticator {
  private static final Logger LOG = LoggerFactory.getLogger(LocalAuthenticator.class);
  private final JsonFileHandler jsonFileHandler;

  @Inject
  public LocalAuthenticator(JsonFileHandler jsonFileHandler) {
    this.jsonFileHandler = jsonFileHandler;
  }

  public String authenticate(String normalizedAuthString) throws UnauthorizedException {
    Login example = new Login();
    example.setAuthString(normalizedAuthString);

    try {
      LoginCollection loginCollection = jsonFileHandler.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME);

      Login login = loginCollection.findItem(example);

      if (login != null) {

        return login.getUserPid();
      }
    } catch (StorageException e) {
      LOG.error("{} cannot be read: {}", LOGIN_COLLECTION_FILE_NAME, e);
    }

    throw new UnauthorizedException("User name and password are unknown.");
  }
}
