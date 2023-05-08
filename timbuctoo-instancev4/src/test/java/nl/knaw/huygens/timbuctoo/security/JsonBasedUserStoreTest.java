package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.exceptions.UserCreationException;
import nl.knaw.huygens.timbuctoo.util.FileHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.security.JsonBasedUserStoreStubs.forFile;
import static nl.knaw.huygens.hamcrest.OptionalPresentMatcher.present;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

public class JsonBasedUserStoreTest {

  public Path usersFile;
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private JsonBasedUserStore instance;

  @BeforeEach
  public void setUp() throws Exception {
    usersFile = FileHelpers.makeTempFilePath(false);
    User user = User.create("", "pidOfKnownUser");
    User userWithoutPid = User.create(null, null);
    User[] users = {user, userWithoutPid};
    OBJECT_MAPPER.writeValue(usersFile.toFile(), users);

    instance = forFile(usersFile);
  }

  @AfterEach
  public void tearDown() throws Exception {
    Files.delete(usersFile);
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

  @Test
  public void ifTheDatabaseContainsOneUserWithNullAsPidThatUserIsReturnedWhenQueryingForNull()
    throws AuthenticationUnavailableException {
    Optional<User> knownUser = instance.userFor(null);

    assertThat(knownUser, is(present()));
  }

  @Test
  public void userForThrowsAnAuthenticationUnavailableExceptionWhenTheUsersFileCannotBeRead() {
    JsonBasedUserStore instance = forFile(Paths.get("nonExistingUserFile"));

    assertThrows(AuthenticationUnavailableException.class, () -> instance.userFor("pid"));
  }

  @Test
  public void createUserAddsAUserToTheUsersFile() throws Exception {
    User createdUser = instance.createUser("pidOfOtherUser", "email", "givenName", "surname", "organization");

    Optional<User> user = instance.userForId(createdUser.getId());
    assertThat(user, is(present()));
    assertThat(user.get().getPersistentId(), is("pidOfOtherUser"));
  }

  @Test
  public void createUserLetsTheUserBeCreatedOnlyOnce() throws Exception {
    User user1 = instance.createUser("pid", "email", "givenName", "surname", "organization");
    User user2 = instance.createUser("pid", "email", "givenName", "surname", "organization");

    assertThat(user1.getId(), is(user2.getId()));

  }

  @Test
  public void createUserThrowsAUserCreationExceptionWhenTheUsersFileCannotBeRead() {
    Path nonExistingUsersFile = Paths.get("src", "test", "resources", "users1.json");
    JsonBasedUserStore instance = forFile(nonExistingUsersFile);

    assertThrows(UserCreationException.class, () ->
        instance.createUser("pid", "email", "givenName", "surname", "organization"));
  }

}
