package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class JsonBasedAuthenticator {
  public static final Logger LOG = LoggerFactory.getLogger(JsonBasedAuthenticator.class);
  private final ObjectMapper objectMapper;
  private final Path loginsFile;
  private final String algorithm;

  public JsonBasedAuthenticator(Path loginsFile) {
    this(loginsFile, "SHA-256");
  }

  public JsonBasedAuthenticator(Path loginsFile, String encryptionAlgorithm) {
    objectMapper = new ObjectMapper();
    this.loginsFile = loginsFile;
    this.algorithm = encryptionAlgorithm;
  }

  public String authenticate(String username, String password) throws LocalLoginUnavailableException {
    try {
      List<Login> logins = objectMapper.readValue(loginsFile.toFile(), new TypeReference<List<Login>>() {
      });

      Optional<Login> first = logins.stream().filter(login -> login.getUsername().equals(username)).findFirst();

      if (first.isPresent()) {

        Login login = first.get();

        return isCorrectPassword(password, login) ? login.getUserPid() : null;
      }
    } catch (IOException e) {
      LOG.error("Could not read \"{}\"", loginsFile.toAbsolutePath());
      LOG.error("Exception", e);

      throw new LocalLoginUnavailableException(e.getMessage());
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Encryption algorithm can not be found.", e);
      throw new LocalLoginUnavailableException(e.getMessage());
    }
    return null;
  }

  private boolean isCorrectPassword(String password, Login login) throws NoSuchAlgorithmException {
    String encryptedPassword = encryptPassword(password, login.getSalt());

    return encryptedPassword.equals(login.getPassword());
  }

  // inspired by https://www.owasp.org/index.php/Hashing_Java
  private String encryptPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
    messageDigest.reset();
    messageDigest.update(salt);
    byte[] encryptedAuth = messageDigest.digest(password.getBytes());
    return new String(encryptedAuth);
  }
}
