package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JsonBasedAuthenticator implements Authenticator, LoginCreator {
  private static final Charset UTF8 = Charset.forName("UTF-8");
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

  @Override
  public Optional<String> authenticate(String username, String password) throws LocalLoginUnavailableException {
    try {
      List<Login> logins = getLogins();

      Optional<Login> first = logins.stream().filter(login -> login.getUsername().equals(username)).findFirst();

      if (first.isPresent()) {
        Login login = first.get();

        return isCorrectPassword(password, login) ? Optional.of(login.getUserPid()) : Optional.empty();
      }
    } catch (IOException e) {
      LOG.error("Could not read \"{}\"", loginsFile.toAbsolutePath());
      LOG.error("Exception", e);
      throw new LocalLoginUnavailableException(e.getMessage());
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Encryption algorithm can not be found.", e);
      throw new LocalLoginUnavailableException(e.getMessage());
    }
    return Optional.empty();
  }

  private boolean isCorrectPassword(String password, Login login) throws NoSuchAlgorithmException {
    byte[] encryptedPassword = encryptPassword(password, login.getSalt());

    return new String(encryptedPassword, UTF8).equals(new String(login.getPassword(), UTF8));
  }

  // inspired by https://www.owasp.org/index.php/Hashing_Java
  private byte[] encryptPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
    messageDigest.reset();
    messageDigest.update(salt);
    byte[] encryptedAuth = messageDigest.digest(password.getBytes());
    return encryptedAuth;
  }

  @Override
  public void createLogin(String userPid, String userName, String password, String givenName, String surname,
                          String email, String organization) throws LoginCreationException {

    try {
      List<Login> logins = getLogins();

      if (containsLoginForUserName(logins, userName)) {
        LOG.warn("Already contains a login for userName '{}'. No login added.", userName);
        return;
      }

      if (logins.stream().anyMatch(login -> Objects.equals(login.getUserPid(), userPid))) {
        LOG.warn("Already contains a login for userPid '{}'. No login added", userPid);
        return;
      }

      Login login = create(userPid, userName, password, givenName, surname, email, organization);
      logins.add(login);

      synchronized (loginsFile) {
        objectMapper.writeValue(loginsFile.toFile(), logins.toArray(new Login[logins.size()]));
      }

    } catch (NoSuchAlgorithmException e) {
      LOG.error("Encryption algorithm can not be found.", e);
      throw new LoginCreationException(e.getMessage());
    } catch (IOException e) {
      LOG.error("Could not read \"{}\"", loginsFile.toAbsolutePath());
      LOG.error("Exception", e);
      throw new LoginCreationException(e.getMessage());
    }
  }

  private List<Login> getLogins() throws IOException {
    List<Login> logins;
    synchronized (loginsFile) {
      logins = objectMapper.readValue(loginsFile.toFile(), new TypeReference<List<Login>>() {
      });
    }
    return logins;
  }

  private boolean containsLoginForUserName(List<Login> logins, String userName) {
    return logins.stream().anyMatch(login -> Objects.equals(login.getUsername(), userName));
  }

  public Login create(String userPid, String userName, String password, String givenName, String surname, String email,
                      String organization) throws NoSuchAlgorithmException {
    byte[] salt = createSalt();
    byte[] encryptedPassword = encryptPassword(password, salt);

    Login login = new Login(userPid, userName, encryptedPassword, salt);
    login.setGivenName(givenName);
    login.setSurName(surname);
    login.setEmailAddress(email);
    login.setOrganization(organization);
    return login;
  }

  private byte[] createSalt() {
    return UUID.randomUUID().toString().getBytes();
  }
}
