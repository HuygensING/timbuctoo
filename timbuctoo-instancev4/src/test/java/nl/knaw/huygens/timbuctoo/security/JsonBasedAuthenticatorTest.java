package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.security.dto.Login;
import nl.knaw.huygens.timbuctoo.security.exceptions.LocalLoginUnavailableException;
import nl.knaw.huygens.timbuctoo.security.exceptions.LoginCreationException;
import nl.knaw.huygens.timbuctoo.util.FileHelpers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static nl.knaw.huygens.hamcrest.OptionalPresentMatcher.present;
import static nl.knaw.huygens.timbuctoo.security.JsonBasedAuthenticatorStubs.backedByFile;
import static nl.knaw.huygens.timbuctoo.security.JsonBasedAuthenticatorStubs.throwingWithAlgorithm;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

public class JsonBasedAuthenticatorTest {

  public static final String KNOWN_USER = "knownUser";
  public static Path LOGINS_FILE;
  public static final String CORRECT_PASSWORD = "correctPassword";
  private JsonBasedAuthenticator instance;

  @BeforeEach
  public void setUp() throws Exception {
    LOGINS_FILE = FileHelpers.getFileFromResource(JsonBasedAuthenticatorTest.class, "logins.json");
    instance = backedByFile(LOGINS_FILE);
  }

  @Test
  public void authenticateReturnsNullWhenTheUsernameIsUnknown() throws Exception {
    Optional<String> authenticate = instance.authenticate("unknownUser", "password");
    assertThat(authenticate, is(not(present())));
  }

  @Test
  public void authenticateReturnsThePidWhenTheUsernameAndPasswordAreCorrect() throws Exception {
    Optional<String> authenticate = instance.authenticate(KNOWN_USER, CORRECT_PASSWORD);
    assertThat(authenticate, is(present()));
  }

  @Test
  public void authenticateReturnsNullWhenThePasswordIsIncorrect() throws Exception {
    Optional<String> authenticate = instance.authenticate(KNOWN_USER, "incorrectPassword");
    assertThat(authenticate, is(not(present())));
  }

  @Test
  public void authenticateThrowsALocalLoginUnavailableExceptionWhenTheLoginsFileCouldBeRead() {
    JsonBasedAuthenticator instance = backedByFile(Paths.get("unavailableLoginsFile"));

    assertThrows(LocalLoginUnavailableException.class, () -> instance.authenticate("user", "password"));
  }

  @Test
  public void constructorThrowsNoSuchAlgorithmWhenTheAlgorithmIsNotAvailable() {
    assertThrows(NoSuchAlgorithmException.class, () -> throwingWithAlgorithm(LOGINS_FILE, "bogusAlgorithm"));
  }

  @Test
  public void createLoginAddsALoginToTheLoginsFile() throws Exception {
    Login[] logins = new Login[0];
    Path emptyLoginsFile = FileHelpers.makeTempFilePath(true);
    new ObjectMapper().writeValue(emptyLoginsFile.toFile(), logins);
    JsonBasedAuthenticator instance = backedByFile(emptyLoginsFile);

    instance.createLogin("userPid", "userName", "password", "givenName", "surname", "email", "org");

    Optional<String> authenticate = instance.authenticate("userName", "password");
    assertThat(authenticate, is(present()));

    Files.delete(emptyLoginsFile);
  }

  @Test
  public void createLoginIgnoresTheAdditionOfLoginOfAKnownUserPid() throws Exception {
    Login[] logins = new Login[0];
    Path emptyLoginsFile = FileHelpers.makeTempFilePath(true);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValue(emptyLoginsFile.toFile(), logins);
    JsonBasedAuthenticator instance = backedByFile(emptyLoginsFile);

    String userPid = "userPid";
    instance.createLogin(userPid, "userName", "password", "givenName", "surname", "email", "org");
    instance.createLogin(userPid, "userName1", "password1", "givenName1", "surname1", "email1", "org1");

    List<Login> loginList = objectMapper.readValue(emptyLoginsFile.toFile(), new TypeReference<>() {
    });
    long count = loginList.stream().filter(login -> Objects.equals(login.getUserPid(), userPid)).count();
    assertThat(count, is(1L));

    Files.delete(emptyLoginsFile);
  }

  @Test
  public void createLoginIgnoresTheAdditionOfLoginOfAKnownUserName() throws Exception {
    Login[] logins = new Login[0];
    Path emptyLoginsFile = FileHelpers.makeTempFilePath(true);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValue(emptyLoginsFile.toFile(), logins);
    JsonBasedAuthenticator instance = backedByFile(emptyLoginsFile);

    String userName = "userName";
    instance.createLogin("userPid", userName, "password", "givenName", "surname", "email", "org");
    instance.createLogin("userPid2", userName, "password1", "givenName2", "surname2", "email2", "org2");

    List<Login> loginList = objectMapper.readValue(emptyLoginsFile.toFile(), new TypeReference<>() {
    });
    long count = loginList.stream().filter(login -> Objects.equals(login.getUsername(), userName)).count();
    assertThat(count, is(1L));

    Files.delete(emptyLoginsFile);
  }

  @Test
  public void createLoginThrowsLoginCreationExceptionWhenTheLoginsFileCannotBeRead() throws Exception {
    Assertions.assertThrows(LoginCreationException.class, () -> {
      Path pathToNonExistingFile = FileHelpers.makeTempFilePath(false);
      JsonBasedAuthenticator instance = backedByFile(pathToNonExistingFile);

      instance.createLogin("userPid", "userName", "password", "givenName", "surname", "email", "org");
    });
  }
}
