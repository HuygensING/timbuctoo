package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.OptionalPresentMatcher.present;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JsonBasedAuthenticatorTest {

  public static final String KNOWN_USER = "knownUser";
  public static final Path LOGINS_FILE = Paths.get(
    "src", "test", "resources", "nl", "knaw", "huygens", "timbuctoo", "security", "logins.json"
  );
  public static final String CORRECT_PASSWORD = "correctPassword";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private JsonBasedAuthenticator instance;

  @Before
  public void setUp() throws Exception {
    instance = new JsonBasedAuthenticator(LOGINS_FILE);
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
  public void authenticateThrowsALocalLoginUnavailableExceptionWhenTheLoginsFileCouldBeRead()
    throws LocalLoginUnavailableException {
    JsonBasedAuthenticator instance = new JsonBasedAuthenticator(Paths.get("unavailableLoginsFile"));

    expectedException.expect(LocalLoginUnavailableException.class);

    instance.authenticate("user", "password");
  }

  @Test
  public void authenticateThrowsALocalLoginUnavailableExceptionWhenTheEncryptionAlgorithmIsUnavailable()
    throws LocalLoginUnavailableException {
    JsonBasedAuthenticator instance = new JsonBasedAuthenticator(LOGINS_FILE, "bogusAlgorithm");

    expectedException.expect(LocalLoginUnavailableException.class);

    instance.authenticate(KNOWN_USER, CORRECT_PASSWORD);
  }

  @Test
  public void createLoginAddsALoginToTheLoginsFile() throws Exception {
    Login[] logins = new Login[0];
    Path emptyLoginsFile = Paths.get("src", "test", "resources", "logins1.json");
    new ObjectMapper().writeValue(emptyLoginsFile.toFile(), logins);
    JsonBasedAuthenticator instance = new JsonBasedAuthenticator(emptyLoginsFile);

    instance.createLogin("userPid", "userName", "password", "givenName", "surname", "email", "org");

    Optional<String> authenticate = instance.authenticate("userName", "password");
    assertThat(authenticate, is(present()));

    Files.delete(emptyLoginsFile);
  }

  @Test
  public void createLoginIgnoresTheAdditionOfLoginOfAKnownUserPid() throws Exception {
    Login[] logins = new Login[0];
    Path emptyLoginsFile = Paths.get("src", "test", "resources", "logins1.json");
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValue(emptyLoginsFile.toFile(), logins);
    JsonBasedAuthenticator instance = new JsonBasedAuthenticator(emptyLoginsFile);

    String userPid = "userPid";
    instance.createLogin(userPid, "userName", "password", "givenName", "surname", "email", "org");
    instance.createLogin(userPid, "userName1", "password1", "givenName1", "surname1", "email1", "org1");

    List<Login> loginList = objectMapper.readValue(emptyLoginsFile.toFile(), new TypeReference<List<Login>>() {
    });
    long count = loginList.stream().filter(login -> Objects.equals(login.getUserPid(), userPid)).count();
    assertThat(count, is(1L));

    Files.delete(emptyLoginsFile);
  }

  @Test
  public void createLoginIgnoresTheAdditionOfLoginOfAKnownUserName() throws Exception {
    Login[] logins = new Login[0];
    Path emptyLoginsFile = Paths.get("src", "test", "resources", "logins1.json");
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValue(emptyLoginsFile.toFile(), logins);
    JsonBasedAuthenticator instance = new JsonBasedAuthenticator(emptyLoginsFile);

    String userName = "userName";
    instance.createLogin("userPid", userName, "password", "givenName", "surname", "email", "org");
    instance.createLogin("userPid2", userName, "password1", "givenName2", "surname2", "email2", "org2");

    List<Login> loginList = objectMapper.readValue(emptyLoginsFile.toFile(), new TypeReference<List<Login>>() {
    });
    long count = loginList.stream().filter(login -> Objects.equals(login.getUsername(), userName)).count();
    assertThat(count, is(1L));

    Files.delete(emptyLoginsFile);
  }

  @Test(expected = LoginCreationException.class)
  public void createLoginThrowsLoginCreationExceptionWhenThePasswordCannotBeEncrypted() throws Exception {
    Login[] logins = new Login[0];
    Path emptyLoginsFile = Paths.get("src", "test", "resources", "logins1.json");
    new ObjectMapper().writeValue(emptyLoginsFile.toFile(), logins);
    JsonBasedAuthenticator instance = new JsonBasedAuthenticator(emptyLoginsFile, "bogusAlgorithm");

    try {
      instance.createLogin("userPid", "userName", "password", "givenName", "surname", "email", "org");
    } finally {
      Files.delete(emptyLoginsFile);
    }
  }

  @Test(expected = LoginCreationException.class)
  public void createLoginThrowsLoginCreationExceptionWhenTheLoginsFileCannotBeRead() throws Exception {
    Path pathToNonExistingFile = Paths.get("src", "test", "resources", "logins1.json");
    JsonBasedAuthenticator instance = new JsonBasedAuthenticator(pathToNonExistingFile);

    instance.createLogin("userPid", "userName", "password", "givenName", "surname", "email", "org");
  }
}
