package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.storage.file.LoginCollection.LOGIN_COLLECTION_FILE_NAME;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.inject.Inject;

public class LocalAuthenticator {
  private static final Logger LOG = LoggerFactory.getLogger(LocalAuthenticator.class);
  private final JsonFileHandler jsonFileHandler;
  private final PasswordEncrypter passwordEncrypter;

  @Inject
  public LocalAuthenticator(JsonFileHandler jsonFileHandler, PasswordEncrypter passwordEncrypter) {
    this.jsonFileHandler = jsonFileHandler;
    this.passwordEncrypter = passwordEncrypter;
  }

  /**
   * A class that checks if the user can be authenticated. 
   * @param authString a base64 encoded "user:password"
   * @return the persistentId of the user.
   * @throws UnauthorizedException when the user name and password combination is unknown.
   */
  public String authenticate(String authString) throws UnauthorizedException {
    if (StringUtils.isBlank(authString)) {
      throw new UnauthorizedException("User name and password are unknown.");
    }

    Login example = new Login();
    String decodedAuthString = decodeAuthString(authString);
    String userName = getUserName(decodedAuthString);

    example.setUserName(userName);

    try {
      LoginCollection loginCollection = jsonFileHandler.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME);

      Login login = loginCollection.findItem(example);

      if (login != null) {
        String password = encryptPassword(decodedAuthString, login.getSalt());

        if (Objects.equal(password, login.getPassword())) {
          return login.getUserPid();
        }

      }
    } catch (StorageException e) {
      LOG.error("{} cannot be read: {}", LOGIN_COLLECTION_FILE_NAME, e);
    }

    throw new UnauthorizedException("User name and password are unknown.");
  }

  private String encryptPassword(String decodedAuthString, byte[] salt) {
    return passwordEncrypter.encryptPassword(getPassword(decodedAuthString), salt);
  }

  private String getPassword(String decodedAuthString) {
    // TODO: if decodedAuthString is empty generate a randomString.
    return decodedAuthString.split(":")[1];
  }

  private String decodeAuthString(String authString) {
    // TODO: if authString is null return an empty String.
    return new String(Base64.decodeBase64(authString.getBytes()));
  }

  private String getUserName(String decodedAuthString) {
    // TODO: if decodedAuthString is empty generate a randomString.
    return decodedAuthString.split(":")[0];
  }
}
