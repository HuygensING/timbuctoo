package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;
import static nl.knaw.huygens.timbuctoo.util.OptionalPresentMatcher.present;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class VreAuthorizationCollectionTest {

  public static final Path AUTHORIZATIONS_FOLDER = Paths.get("src", "test", "resources");
  public static final String VRE = "vre";
  public static final Path VRE_AUTH_PATH = AUTHORIZATIONS_FOLDER.resolve(String.format("%s.json", VRE));

  public static final String USER_ID = "USER000000000001";
  private VreAuthorizationCollection instance;
  private ObjectMapper objectMapper;

  @Before
  public void setup() throws Exception {
    VreAuthorization[] authorizations = {new VreAuthorization(VRE, USER_ID, "USER")};
    File file = VRE_AUTH_PATH.toFile();
    objectMapper = new ObjectMapper();
    objectMapper.writeValue(file, authorizations);

    instance = new VreAuthorizationCollection(AUTHORIZATIONS_FOLDER);
  }

  @After
  public void teardown() throws Exception {
    Files.delete(VRE_AUTH_PATH);
  }

  @Test
  public void authorizationForReturnsAnOptionalOfTheFoundVreAuthorization() throws Exception {
    Optional<VreAuthorization> vreAuthorization = instance.authorizationFor(VRE, USER_ID);

    assertThat(vreAuthorization, is(present()));
  }

  @Test
  public void authorizationForReturnsAnEmptyOptionalIfTheVreAuthorizationIsNotFound() throws Exception {
    Optional<VreAuthorization> vreAuthorization = instance.authorizationFor(VRE, "unknownUser");

    assertThat(vreAuthorization, is(not(present())));
  }

  @Test
  public void authorizationForReturnsAnEmptyOptionalIfTheVreAuthorizationsFileDoesNotExist() throws Exception {
    Optional<VreAuthorization> vreAuthorization = instance.authorizationFor("nonExisting", USER_ID);

    assertThat(vreAuthorization, is(not(present())));
  }

  // TODO: add a test to check how to handle an IOException

  @Test
  public void addAuthorizationForAddsANewAuthorizationToTheVreForTheUser() throws Exception {
    String unknownUser = "unknownUser";
    Optional<VreAuthorization> authorization = instance.authorizationFor(VRE, unknownUser);
    assertThat(authorization, is(not(present())));

    instance.addAuthorizationFor(VRE, unknownUser, UNVERIFIED_USER_ROLE);

    Optional<VreAuthorization> authorization1 = instance.authorizationFor(VRE, unknownUser);
    assertThat(authorization1, is(present()));
  }

  @Test
  public void addAuthorizationReturnsTheAddedAuthorization() throws Exception {
    String unknownUser = "unknownUser";

    VreAuthorization vreAuthorization = instance.addAuthorizationFor(VRE, unknownUser, UNVERIFIED_USER_ROLE);
    Optional<VreAuthorization> authorization = instance.authorizationFor(VRE, unknownUser);

    assertThat(vreAuthorization, is(authorization.get()));
  }

  @Test
  public void addAuthorizationIgnoresTheSecondAdditionForUserToAVre() throws Exception {
    String unknownUser = "unknownUser";

    instance.addAuthorizationFor(VRE, unknownUser, UNVERIFIED_USER_ROLE);
    VreAuthorization authorization2 = instance.addAuthorizationFor(VRE, unknownUser, ADMIN_ROLE);

    assertThat(authorization2.getRoles(), contains(UNVERIFIED_USER_ROLE));

  }

  @Test
  public void addAuthorizationCreatesANewFileForAnUnknownVre() throws Exception {
    String newVre = "newVRE";
    Optional<VreAuthorization> authorization = instance.authorizationFor(newVre, USER_ID);
    assertThat(authorization, is(not(present())));

    VreAuthorization createAuthorization = instance.addAuthorizationFor(newVre, USER_ID, UNVERIFIED_USER_ROLE);

    assertThat(createAuthorization, is(not(nullValue())));
    Optional<VreAuthorization> authorization1 = instance.authorizationFor(newVre, USER_ID);
    assertThat(authorization1, is(present()));

    // Teardown
    Files.delete(AUTHORIZATIONS_FOLDER.resolve(String.format("%s.json", newVre)));
  }
}
