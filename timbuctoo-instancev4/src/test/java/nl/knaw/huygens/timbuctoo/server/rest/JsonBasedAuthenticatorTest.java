package nl.knaw.huygens.timbuctoo.server.rest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.server.rest.OptionalPresentMatcher.present;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JsonBasedAuthenticatorTest {

  public static final String KNOWN_USER = "knownUser";
  public static final Path LOGINS_FILE = Paths.get("src", "test", "resources", "logins.json");
  public static final String CORRECT_PASSWORD = "correctPassword";
  private JsonBasedAuthenticator instance;

  @Before
  public void setUp() throws Exception {
    instance = new JsonBasedAuthenticator(LOGINS_FILE);
  }

  @Test
  public void authenticateReturnsNullWhenTheUsernameIsUnknown() throws LocalLoginUnavailableException {
    Optional<String> pid = instance.authenticate("unknownUser", "password");

    assertThat(pid, is(not(present())));
  }

  @Test
  public void authenticateReturnsThePidWhenTheUsernameAndPasswordAreCorrect() throws LocalLoginUnavailableException {
    Optional<String> pid = instance.authenticate(KNOWN_USER, CORRECT_PASSWORD);

    assertThat(pid, is(present()));
  }

  @Test
  public void authenticateReturnsNullWhenThePasswordIsIncorrect() throws LocalLoginUnavailableException {
    Optional<String> pid = instance.authenticate(KNOWN_USER, "incorrectPassword");

    assertThat(pid, is(not(present())));
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
}
