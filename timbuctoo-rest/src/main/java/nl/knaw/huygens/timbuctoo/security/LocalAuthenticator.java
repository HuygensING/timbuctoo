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

// class inspired by https://www.owasp.org/index.php/Hashing_Java

public class LocalAuthenticator {
  // used when the authString is empty.
  private static final String SUBSTITUTE_AUTH_STRING = "xxxxxxxxxxxxxxxxxxxxxx:xxxxxxxxxxxxxxxxxxxxxxxxxx";
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
  public Login authenticate(String authString) throws UnauthorizedException {

    String decodedAuthString = decodeAuthString(authString);
    String userName = getUserName(decodedAuthString);
    String password = getPassword(decodedAuthString);

    Login example = new Login();
    example.setUserName(userName);

    Login login = null;
    boolean userIsKnown = false;

    try {
      LoginCollection loginCollection = jsonFileHandler.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME);

      login = loginCollection.findItem(example);

    } catch (StorageException e) {
      LOG.error("{} cannot be read: {}", LOGIN_COLLECTION_FILE_NAME, e);
    }

    if (login == null) {
      login = new Login();
      login.setSalt(new byte[] {});
    } else {
      userIsKnown = true;
    }

    String encryptedPassword = encryptPassword(password, login.getSalt());

    if (userIsKnown && Objects.equal(encryptedPassword, login.getPassword())) {
      return login;
    }

    throw new UnauthorizedException("User name and password are unknown.");
  }

  private String encryptPassword(String password, byte[] salt) {
    return passwordEncrypter.encryptPassword(password, salt);
  }

  private String decodeAuthString(String authString) {
    if (StringUtils.isBlank(authString)) {
      return SUBSTITUTE_AUTH_STRING;
    }

    return new String(Base64.decodeBase64(authString.getBytes()));
  }

  private String getUserName(String decodedAuthString) {
    if (isValidAuthString(decodedAuthString)) {
      return decodedAuthString.split(":")[0];
    }

    return "";
  }

  private boolean isValidAuthString(String decodedAuthString) {
    return decodedAuthString.split(":").length > 1;
  }

  private String getPassword(String decodedAuthString) {
    if (isValidAuthString(decodedAuthString)) {
      return decodedAuthString.split(":")[1];
    }
    return "";
  }
}
