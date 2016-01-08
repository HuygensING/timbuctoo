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

public class JsonBasedUserStoreTest {

  public static final Path USERS_FILE = Paths.get("src", "test", "resources", "users.json");
  private JsonBasedUserStore instance;

  @Before
  public void setUp() throws Exception {
    instance = new JsonBasedUserStore(USERS_FILE);
  }

  @Test
  public void userForReturnsAnEmptyOptionalForAnUnknownUser() throws AuthenticationUnavailableException {
    Optional<User> unknownUser = instance.userFor("pidOfUnknownUser");

    assertThat(unknownUser, is(not(present())));
  }

  @Test
  public void userForReturnsAnOptionalWithAUserForAKnownUser() throws AuthenticationUnavailableException {
    Optional<User> knownUser = instance.userFor("pidOfKnownUser");

    assertThat(knownUser, is(present()));
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test()
  public void userForThrowsAnAuthenticationUnavailableExceptionWhenTheUsersFileCannotBeRead()
    throws AuthenticationUnavailableException {
    JsonBasedUserStore instance = new JsonBasedUserStore(Paths.get("nonExistingUserFile"));

    expectedException.expect(AuthenticationUnavailableException.class);

    instance.userFor("pid");
  }
}
